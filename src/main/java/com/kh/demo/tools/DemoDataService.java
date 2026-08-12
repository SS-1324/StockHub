package com.kh.demo.tools;

import com.kh.demo.brokerage.dto.*;
import com.kh.demo.brokerage.mapper.*;
import com.kh.demo.member.dto.MemberDto;
import com.kh.demo.member.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/*
*   DemoDataService : 가상 증권사 데모 데이터 생성기의 실제 로직.
*
*   목적: '진짜 파트너 증권사가 1년치 이력을 이미 갖고 있었다'는 전제를 데이터로 구현.
*   1회성 백필 도구이며(com.kh.demo.tools.DemoDataGenerator에서 수동 실행), 실제 서비스 API로는 노출하지 않는다.
* */
@Service
public class DemoDataService {

    @Autowired private BrokerageMapper brokerageMapper;
    @Autowired private StockMapper stockMapper;
    @Autowired private FinancialProductMapper financialProductMapper;
    @Autowired private AccountMapper accountMapper;
    @Autowired private HoldingMapper holdingMapper;
    @Autowired private TradeMapper tradeMapper;
    @Autowired private ProductHoldingMapper productHoldingMapper;
    @Autowired private ProductTransactionMapper productTransactionMapper;
    @Autowired private CashTransactionMapper cashTransactionMapper;
    @Autowired private AssetSnapshotMapper assetSnapshotMapper;
    @Autowired private MemberMapper memberMapper;
    @Autowired private StockPriceHistoryMapper stockPriceHistoryMapper;

    // 시드 고정 -> 재실행해도 같은 결과가 나와서 "손보면서 반복"하기 편함
    private final Random random = new Random(20260805L);

    // 포트폴리오 완성본 데모용 - 이 회원에게는 아주 많은 거래/보유 이력을 가진 고수익 "메가 계좌"를 만든다
    private static final String MEGA_ACCOUNT_MEMBER_ID = "ghdrlfehd";
    private static final long MEGA_ACCOUNT_PRINCIPAL = 500_000_000L;

    private enum Activity { QUIET, MODERATE, ACTIVE, MEGA }
    private enum Performance { GOOD, NEUTRAL, BAD }

    private static final String[] DEPOSIT_MEMOS = {"급여 이체", "용돈 입금", "이체 입금"};
    private static final String[] WITHDRAW_MEMOS = {"생활비 출금", "경조사비 출금", "카드값 출금"};

    @Transactional
    public void generate() {
        seedFinancialProductsIfEmpty();
        List<AccountDto> newDemoAccounts = seedUnlinkedDemoAccountsIfMissing();
        seedAccountsForUnaccountedMembers();

        List<AccountDto> targets = accountMapper.selectAllNonAdminAccounts();
        List<StockDto> stocks = stockMapper.selectAllStocks();
        List<FinancialProductDto> products = financialProductMapper.selectProducts(null, null);

        LocalDateTime historyStart = LocalDateTime.now().minusYears(1);
        Map<String, double[]> globalStockPaths = buildAndPersistStockPriceHistory(stocks, historyStart);
        // 시세가 없는(0원) 종목은 거래가 애초에 성립하지 않으므로 계좌 배정 대상에서도 제외한다
        List<StockDto> pricedStocks = stocks.stream()
                .filter(s -> globalStockPaths.containsKey(s.getStockCode()))
                .toList();

        List<Activity> activityPool = pool(Activity.values(), targets.size());
        List<Performance> performancePool = pool(Performance.values(), targets.size());
        // Activity.MEGA는 무작위 풀이 아니라 특정 회원 전용이므로 풀에서는 제외한다
        activityPool.replaceAll(a -> a == Activity.MEGA ? Activity.ACTIVE : a);
        Collections.shuffle(activityPool, random);
        Collections.shuffle(performancePool, random);

        for (int i = 0; i < targets.size(); i++) {
            AccountDto account = targets.get(i);
            Activity activity = activityPool.get(i);
            Performance performance = performancePool.get(i);
            if (MEGA_ACCOUNT_MEMBER_ID.equals(account.getMemberId())) {
                // 매수 시점을 pickCheapWeek로 강제 지정하므로(아래 guaranteedCheapBuys) performance/bias는 관여하지 않는다
                activity = Activity.MEGA;
                accountMapper.updateBalance(account.getAccountId(), MEGA_ACCOUNT_PRINCIPAL);
                account.setBalance(MEGA_ACCOUNT_PRINCIPAL);
            }
            regenerateHistory(account, activity, performance, pricedStocks, products, globalStockPaths, historyStart);
        }

        printSummary(targets.size(), newDemoAccounts);
    }

    private <T> List<T> pool(T[] values, int size) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(values[i % values.length]);
        }
        return list;
    }

    // 종목별 시세 흐름을 전체에서 딱 한 번만 만들어 stock_price_history에 남기고, 모든 계좌가 이 흐름을 공유한다
    private Map<String, double[]> buildAndPersistStockPriceHistory(List<StockDto> allStocks, LocalDateTime historyStart) {
        Map<String, double[]> paths = new LinkedHashMap<>();
        List<StockPriceHistoryDto> rows = new ArrayList<>();
        for (StockDto s : allStocks) {
            if (s.getCurrentPrice() == null || s.getCurrentPrice() <= 0) {
                continue; // 아직 시세가 없는 종목 - 상장 전 취급으로 건너뜀
            }
            double[] path = PriceWalk.generate(s.getCurrentPrice(), random, 0.06);
            paths.put(s.getStockCode(), path);
            for (int week = 0; week <= PriceWalk.WEEKS; week++) {
                rows.add(new StockPriceHistoryDto(null, s.getStockCode(),
                        Math.round(path[week]), historyStart.plusWeeks(week)));
            }
        }
        stockPriceHistoryMapper.deleteAll();
        if (!rows.isEmpty()) {
            stockPriceHistoryMapper.insertBatch(rows);
        }
        return paths;
    }

    // ==================== 1. 금융상품 카탈로그 ====================

    private void seedFinancialProductsIfEmpty() {
        if (financialProductMapper.countAllProducts() > 0) {
            return;
        }
        Map<String, Long> brokerageIdByName = new HashMap<>();
        for (BrokerageDto b : brokerageMapper.selectAllBrokerages()) {
            brokerageIdByName.put(b.getBrokerageName(), b.getBrokerageId());
        }

        insertProduct(brokerageIdByName.get("스톡증권"), "BOND", "스톡 안정형 국공채펀드",
                "국고채·통안채 위주로 운용하는 저위험 채권형 상품", "10120.00", null);
        insertProduct(brokerageIdByName.get("스톡증권"), "FUND", "스톡 글로벌 성장주 펀드",
                "미국·유럽 대형 성장주에 분산 투자하는 액티브 펀드", "12480.00", null);
        insertProduct(brokerageIdByName.get("스톡증권"), "ELS", "스톡 조기상환 ELS 1호",
                "코스피200/S&P500 기초, 6개월마다 조기상환 조건 평가", "10000.00", LocalDate.now().plusYears(3));

        insertProduct(brokerageIdByName.get("허브증권"), "FUND", "허브 머니마켓펀드",
                "단기 우량채·CP 위주로 운용하는 초저위험 유동성 펀드", "10005.00", null);
        insertProduct(brokerageIdByName.get("허브증권"), "BOND", "허브 우량회사채펀드",
                "신용등급 AA- 이상 회사채로 구성된 채권형 상품", "10850.00", null);
        insertProduct(brokerageIdByName.get("허브증권"), "ELS", "허브 지수연동 ELS 2호",
                "니케이225 기초, 원금비보장형 스텝다운 구조", "10000.00", LocalDate.now().plusYears(2));

        insertProduct(brokerageIdByName.get("KH투자증권"), "FUND", "KH 배당주 펀드",
                "국내 고배당주 중심 인컴형 펀드", "11230.00", null);
        insertProduct(brokerageIdByName.get("KH투자증권"), "BOND", "KH 국공채펀드",
                "국채·지방채 위주 안정형 채권 상품", "10060.00", null);
        insertProduct(brokerageIdByName.get("KH투자증권"), "ELS", "KH 리자드형 ELS 3호",
                "코스피200 기초, 조기 낙인 회피 조건이 붙은 리자드형 구조", "10000.00", LocalDate.now().plusYears(3));
        insertProduct(brokerageIdByName.get("KH투자증권"), "FUND", "KH 테크섹터 펀드",
                "국내외 반도체·플랫폼 기업 집중 투자 펀드", "15320.00", null);
    }

    private void insertProduct(Long brokerageId, String type, String name, String description,
                                String nav, LocalDate maturityDate) {
        if (brokerageId == null) {
            return; // 증권사 시드 데이터가 없으면 조용히 건너뜀(브로커리지 기본 데이터 미실행 상태)
        }
        FinancialProductDto dto = new FinancialProductDto();
        dto.setBrokerageId(brokerageId);
        dto.setProductType(type);
        dto.setProductName(name);
        dto.setDescription(description);
        dto.setNav(new BigDecimal(nav));
        dto.setMaturityDate(maturityDate);
        financialProductMapper.insertProduct(dto);
    }

    // ==================== 2. 미연동 데모 계좌(회원가입 예정자 시나리오) ====================

    private record DemoPersona(String brokerageName, String ownerName, String accountNo, long seedBalance) {}

    private List<AccountDto> seedUnlinkedDemoAccountsIfMissing() {
        List<DemoPersona> personas = List.of(
                // 스톡증권 - 소액/중간/고액
                new DemoPersona("스톡증권", "오지훈", "110-222-000001", 800_000L),
                new DemoPersona("스톡증권", "한소희", "110-222-000002", 15_000_000L),
                new DemoPersona("스톡증권", "배준영", "110-222-000003", 72_000_000L),
                // 허브증권
                new DemoPersona("허브증권", "임다은", "102683300000001", 500_000L),
                new DemoPersona("허브증권", "조성민", "102683300000002", 22_000_000L),
                new DemoPersona("허브증권", "황수아", "102683300000003", 65_000_000L),
                // KH투자증권
                new DemoPersona("KH투자증권", "문가영", "789456120001", 1_200_000L),
                new DemoPersona("KH투자증권", "서준호", "789456120002", 18_500_000L),
                new DemoPersona("KH투자증권", "나은채", "789456120003", 90_000_000L)
        );

        Map<String, Long> brokerageIdByName = new HashMap<>();
        for (BrokerageDto b : brokerageMapper.selectAllBrokerages()) {
            brokerageIdByName.put(b.getBrokerageName(), b.getBrokerageId());
        }

        List<AccountDto> created = new ArrayList<>();
        for (DemoPersona p : personas) {
            Long brokerageId = brokerageIdByName.get(p.brokerageName());
            if (brokerageId == null) {
                continue;
            }
            AccountDto existing = accountMapper.selectAccountByAccountNo(brokerageId, p.accountNo());
            if (existing != null) {
                continue; // 이미 시드됨(재실행) - 새로 만들지 않음, 이력은 아래서 다시 재생성됨
            }
            AccountDto dto = new AccountDto();
            dto.setAccountNo(p.accountNo());
            dto.setOwnerName(p.ownerName());
            dto.setBrokerageId(brokerageId);
            accountMapper.insertAccount(dto);
            dto.setBalance(p.seedBalance());
            accountMapper.updateBalance(dto.getAccountId(), p.seedBalance());
            created.add(accountMapper.selectAccountById(dto.getAccountId()));
        }
        return created;
    }

    // ==================== 2-1. 계좌가 하나도 없는 기존 회원에게 계좌 개설 ====================

    // 시드 잔고를 소액~고액까지 다양하게 순환 배정 (사람 수가 늘어도 안전하게 나머지 연산으로 순환)
    private static final long[] AUTO_SEED_BALANCES = {
            700_000L, 1_500_000L, 3_000_000L, 8_000_000L, 15_000_000L,
            25_000_000L, 40_000_000L, 55_000_000L, 70_000_000L, 20_000_000L
    };

    private void seedAccountsForUnaccountedMembers() {
        List<MemberDto> membersWithoutAccount = memberMapper.selectMembersWithoutAccount();
        if (membersWithoutAccount.isEmpty()) {
            return;
        }
        List<BrokerageDto> brokerages = brokerageMapper.selectAllBrokerages();
        if (brokerages.isEmpty()) {
            return;
        }
        for (int i = 0; i < membersWithoutAccount.size(); i++) {
            MemberDto member = membersWithoutAccount.get(i);
            BrokerageDto brokerage = brokerages.get(i % brokerages.size());
            long seedBalance = AUTO_SEED_BALANCES[i % AUTO_SEED_BALANCES.length];

            AccountDto dto = new AccountDto();
            dto.setAccountNo("AUTO-" + member.getMemberId().toUpperCase() + "-" + brokerage.getBrokerageId());
            dto.setOwnerName(member.getMemberName());
            dto.setBrokerageId(brokerage.getBrokerageId());
            accountMapper.insertAccount(dto);
            accountMapper.updateBalance(dto.getAccountId(), seedBalance);
            accountMapper.linkAccount(dto.getAccountId(), member.getMemberId());
        }
    }

    // ==================== 3. 계좌별 1년 이력 재생성 ====================

    private void regenerateHistory(AccountDto account, Activity activity, Performance performance,
                                    List<StockDto> allStocks, List<FinancialProductDto> allProducts,
                                    Map<String, double[]> globalStockPaths, LocalDateTime start) {
        Long accountId = account.getAccountId();

        // 재실행 안전을 위해 이 계좌의 생성기 대상 데이터를 전부 비우고 다시 채운다
        tradeMapper.deleteTradesByAccount(accountId);
        holdingMapper.deleteHoldingsByAccount(accountId);
        productTransactionMapper.deleteTransactionsByAccount(accountId);
        productHoldingMapper.deleteHoldingsByAccount(accountId);
        cashTransactionMapper.deleteTransactionsByAccount(accountId);

        BrokerageDto brokerage = brokerageMapper.selectBrokerageById(account.getBrokerageId());

        long principal = Math.max(account.getBalance() == null ? 0L : account.getBalance(), 100_000L);
        long[] cash = {principal};
        cashTransactionMapper.insertTransaction(
                cashTx(accountId, "DEPOSIT", principal, cash[0], "초기 입금", start));

        // 메가 계좌는 원금의 일부를 왕복매매 루프가 손대지 못하게 미리 떼어 둔다
        // (그렇지 않으면 연말께 여러 종목의 마지막 보유분 매수가 몰리면서 잔고가 항상 0에 가깝게 소진돼,
        //  연말 목표 수익률 보정(topUpMegaReturn)에 쓸 실탄이 하나도 안 남는 문제가 생긴다)
        boolean roundTripMode = activity == Activity.MEGA;
        long megaReserve = roundTripMode ? principal / 10 : 0;
        cash[0] -= megaReserve;

        int stockCount = switch (activity) {
            case QUIET -> 2 + random.nextInt(2);
            case MODERATE -> 3 + random.nextInt(2);
            case ACTIVE -> 4 + random.nextInt(2);
            case MEGA -> allStocks.size(); // 시세가 있는 종목을 전부 담아 포트폴리오 완성본을 만든다
        };
        int tradesPerStock = switch (activity) {
            case QUIET -> 5 + random.nextInt(3);
            case MODERATE -> 7 + random.nextInt(3);
            case ACTIVE -> 9 + random.nextInt(4);
            case MEGA -> 7 + random.nextInt(3); // 구간을 크게 잡아야(왕복 횟수는 적더라도) 사이클당 낙폭/등락폭을 제대로 포착한다
        };
        int bias = switch (performance) {
            case GOOD -> -1;
            case BAD -> 1;
            case NEUTRAL -> 0;
        };

        List<StockDto> myStocks = pickRandom(allStocks, Math.min(stockCount, allStocks.size()));
        Map<String, double[]> stockPaths = globalStockPaths;

        int productCount = Math.min(random.nextInt(3), allProducts.size()); // 0~2
        List<FinancialProductDto> myProducts = pickRandom(allProducts, productCount);
        Map<Long, double[]> productPaths = new HashMap<>();
        for (FinancialProductDto p : myProducts) {
            productPaths.put(p.getProductId(), PriceWalk.generate(p.getNav().doubleValue(), random, 0.015));
        }

        List<SimEvent> events = buildEvents(start, myStocks, tradesPerStock, stockPaths, bias, myProducts, roundTripMode);
        events.sort(Comparator.comparing(e -> e.at));

        // 왕복매매(메가 계좌)는 매매 한 번마다 잔고 대부분을 태우고 전량 청산해야 실현손익이 복리로 쌓인다.
        // 일반 계좌는 기존처럼 잔고 일부만 담고 일부만 덜어내는 보수적인 비중을 유지한다.
        double buyFractionMin = roundTripMode ? 0.70 : 0.05;
        double buyFractionMax = roundTripMode ? 0.70 : 0.20;
        double sellFractionMin = roundTripMode ? 1.0 : 0.3;
        double sellFractionMax = roundTripMode ? 1.0 : 0.8;

        Map<String, RunningStockPosition> stockPositions = new HashMap<>();
        Map<Long, RunningProductPosition> productPositions = new HashMap<>();

        // 이벤트를 주 단위로 재생하면서, 매주 끝에 "그 시점 총자산" 스냅샷을 남긴다
        // (실제 증권사가 매일 밤 배치로 잔고 스냅샷을 쌓아두는 것과 같은 역할 - 기간별 손익 계산의 기준값이 된다)
        assetSnapshotMapper.deleteSnapshotsByAccount(accountId);
        int eventIndex = 0;
        for (int week = 0; week <= PriceWalk.WEEKS; week++) {
            while (eventIndex < events.size() && weekOf(start, events.get(eventIndex).at) == week) {
                SimEvent e = events.get(eventIndex);
                switch (e.type) {
                    case STOCK_BUY -> handleStockBuy(accountId, e, stockPaths, start, brokerage, cash, stockPositions, buyFractionMin, buyFractionMax);
                    case STOCK_SELL -> handleStockSell(accountId, e, stockPaths, start, brokerage, cash, stockPositions, sellFractionMin, sellFractionMax);
                    case PRODUCT_SUBSCRIBE -> handleSubscribe(accountId, e, myProducts, productPaths, start, cash, productPositions);
                    case PRODUCT_REDEEM -> handleRedeem(accountId, e, productPaths, start, cash, productPositions);
                    case CASH_DEPOSIT -> handleDeposit(accountId, e, cash);
                    case CASH_WITHDRAWAL -> handleWithdrawal(accountId, e, cash);
                }
                eventIndex++;
            }

            // 왕복매매만으로는 시세 변동폭 한계상 100%를 못 넘길 수 있어, 연말에 남은 잔고를
            // 그 해 가장 많이 오른 종목의 연중 최저가에 추가로 태워 목표 수익률을 확실히 채운다
            if (roundTripMode && week == PriceWalk.WEEKS) {
                cash[0] += megaReserve;
                topUpMegaReturn(accountId, principal, cash, stockPositions, myStocks, stockPaths, start, brokerage);
            }

            long totalAssetAtWeek = computeTotalAssetAtWeek(cash[0], stockPositions, stockPaths, productPositions, productPaths, week);
            assetSnapshotMapper.insertSnapshot(accountId, start.plusWeeks(week).toLocalDate(), totalAssetAtWeek);
        }

        finalizeAccount(accountId, myStocks, cash, stockPositions, productPositions);
    }

    // 포트폴리오 완성본을 보여주려면 보유 종목이 여러 개 남아 있어야 하므로, 상위 몇 종목만 남긴다
    private static final int MEGA_TOP_STOCK_COUNT = 5;

    // "메가 계좌" 전용 - 목표 수익률(원금의 120%)에 못 미치면, 상대적으로 덜 오른 보유 종목은 현재가에 정리하고
    // 그 해 가장 많이 오른 상위 몇 종목의 연중 최저가(이미 stock_price_history에 남긴 실제 값)에 나눠 태워 확실하게 채운다.
    // (한 종목에 전부 몰지 않는 이유: 보유 종목이 하나만 남으면 포트폴리오 분석 화면에서 보여줄 게 없어진다)
    private void topUpMegaReturn(Long accountId, long principal, long[] cash,
                                  Map<String, RunningStockPosition> stockPositions,
                                  List<StockDto> myStocks, Map<String, double[]> stockPaths,
                                  LocalDateTime start, BrokerageDto brokerage) {
        long targetAsset = principal * 22 / 10; // 120% 수익 - 매수수수료 등을 감안해 여유를 둔다
        long currentAsset = cash[0];
        for (StockDto s : myStocks) {
            RunningStockPosition pos = stockPositions.get(s.getStockCode());
            if (pos != null && pos.quantity > 0) {
                currentAsset += (long) s.getCurrentPrice() * pos.quantity;
            }
        }
        if (currentAsset >= targetAsset) {
            return;
        }

        // 종목별 "연중 최저가 대비 현재가" 상승폭 순으로 정렬해 상위 몇 개만 남긴다
        record Candidate(StockDto stock, int minWeek, double upside) {}
        List<Candidate> ranked = new ArrayList<>();
        for (StockDto s : myStocks) {
            double[] path = stockPaths.get(s.getStockCode());
            int minWeek = PriceWalk.argExtreme(path, 0, PriceWalk.WEEKS - 1, false);
            double upside = (s.getCurrentPrice() - path[minWeek]) / path[minWeek];
            ranked.add(new Candidate(s, minWeek, upside));
        }
        ranked.sort(Comparator.comparingDouble(Candidate::upside).reversed());
        List<Candidate> keep = ranked.subList(0, Math.min(MEGA_TOP_STOCK_COUNT, ranked.size()));
        Set<String> keepCodes = new HashSet<>();
        for (Candidate c : keep) {
            keepCodes.add(c.stock().getStockCode());
        }

        // 상위권에 들지 못한 종목은 현재가에 정리해 현금으로 바꾼다
        LocalDateTime sellAt = start.plusWeeks(PriceWalk.WEEKS).minusDays(1);
        for (StockDto s : myStocks) {
            if (keepCodes.contains(s.getStockCode())) {
                continue;
            }
            RunningStockPosition pos = stockPositions.get(s.getStockCode());
            if (pos == null || pos.quantity <= 0) {
                continue;
            }
            long qty = pos.quantity;
            int sellPrice = s.getCurrentPrice();
            long amount = (long) sellPrice * qty;
            int fee = feeOf(brokerage, amount);
            cash[0] += amount - fee;
            pos.reduce(qty);
            tradeMapper.insertHistoricalTrade(tradeOf(accountId, s.getStockCode(), "SELL", qty, sellPrice, fee, sellAt));
        }

        // 정리해서 모은 현금을 상승폭이 큰 순서에 가중치를 둬 상위 종목들의 연중 최저가에 나눠 태운다
        double weightSum = keep.stream().mapToDouble(Candidate::upside).sum();
        long budgetPool = cash[0];
        for (Candidate c : keep) {
            if (cash[0] <= 0 || weightSum <= 0) {
                break;
            }
            long share = (long) (budgetPool * (c.upside() / weightSum));
            share = Math.min(share, cash[0]);
            double[] path = stockPaths.get(c.stock().getStockCode());
            int price = (int) Math.round(path[c.minWeek()]);
            if (price <= 0 || share <= 0) {
                continue;
            }
            long quantity = share / price;
            if (quantity <= 0) {
                continue;
            }
            long amount = (long) price * quantity;
            int fee = feeOf(brokerage, amount);
            long totalCost = amount + fee;
            if (totalCost > cash[0]) {
                continue;
            }
            cash[0] -= totalCost;
            stockPositions.computeIfAbsent(c.stock().getStockCode(), k -> new RunningStockPosition()).addBuy(quantity, price, fee);
            LocalDateTime at = start.plusWeeks(c.minWeek()).plusDays(random.nextInt(7));
            tradeMapper.insertHistoricalTrade(tradeOf(accountId, c.stock().getStockCode(), "BUY", quantity, price, fee, at));
        }
    }

    private List<SimEvent> buildEvents(LocalDateTime start, List<StockDto> myStocks, int tradesPerStock,
                                        Map<String, double[]> stockPaths, int bias,
                                        List<FinancialProductDto> myProducts, boolean roundTripMode) {
        List<SimEvent> events = new ArrayList<>();
        for (StockDto s : myStocks) {
            double[] path = stockPaths.get(s.getStockCode());
            if (roundTripMode) {
                events.addAll(buildRoundTripEvents(start, s.getStockCode(), path, tradesPerStock));
                continue;
            }
            for (int t = 0; t < tradesPerStock; t++) {
                boolean isBuy = (t == 0) || random.nextDouble() < 0.55;
                int week = isBuy
                        ? PriceWalk.pickBuyWeek(path, random, bias)
                        : random.nextInt(PriceWalk.WEEKS + 1);
                LocalDateTime at = start.plusWeeks(week).plusDays(random.nextInt(7));
                events.add(new SimEvent(at, isBuy ? SimEvent.Type.STOCK_BUY : SimEvent.Type.STOCK_SELL,
                        s.getStockCode(), null));
            }
        }
        for (FinancialProductDto p : myProducts) {
            int txCount = 1 + random.nextInt(3);
            for (int t = 0; t < txCount; t++) {
                boolean isSubscribe = (t == 0) || random.nextDouble() < 0.7;
                int week = random.nextInt(PriceWalk.WEEKS + 1);
                LocalDateTime at = start.plusWeeks(week).plusDays(random.nextInt(7));
                events.add(new SimEvent(at,
                        isSubscribe ? SimEvent.Type.PRODUCT_SUBSCRIBE : SimEvent.Type.PRODUCT_REDEEM,
                        null, p.getProductId()));
            }
        }
        int extraCashEvents = random.nextInt(4);
        for (int i = 0; i < extraCashEvents; i++) {
            int week = 1 + random.nextInt(PriceWalk.WEEKS - 1);
            LocalDateTime at = start.plusWeeks(week);
            boolean isDeposit = random.nextBoolean();
            events.add(new SimEvent(at, isDeposit ? SimEvent.Type.CASH_DEPOSIT : SimEvent.Type.CASH_WITHDRAWAL,
                    null, null));
        }
        return events;
    }

    // "메가 계좌" 전용 - 52주를 여러 구간으로 쪼개 구간마다 저점 매수 -> 고점 매도를 반복한다.
    // 한 방에 사서 묻어두는 것과 달리, 왕복매매로 실현손익이 계속 재투자되며 복리로 불어나
    // 종목 자체의 연중 등락폭보다 훨씬 큰 최종 수익률을 만들어낸다. 마지막 구간은 팔지 않고 그대로 보유해
    // 최종 보유 종목으로 남긴다.
    private List<SimEvent> buildRoundTripEvents(LocalDateTime start, String stockCode, double[] path, int tradesPerStock) {
        List<SimEvent> events = new ArrayList<>();
        int roundTrips = Math.max(1, (tradesPerStock - 1) / 2);
        int segmentSize = Math.max(1, PriceWalk.WEEKS / (roundTrips + 1));
        int week = 0;
        for (int r = 0; r < roundTrips && week < PriceWalk.WEEKS; r++) {
            int segEnd = Math.min(week + segmentSize, PriceWalk.WEEKS - 1);
            int buyWeek = PriceWalk.argExtreme(path, week, segEnd, false);
            int sellWeek = PriceWalk.argExtreme(path, buyWeek, segEnd, true);
            events.add(stockEvent(start, buyWeek, SimEvent.Type.STOCK_BUY, stockCode));
            if (sellWeek > buyWeek) {
                events.add(stockEvent(start, sellWeek, SimEvent.Type.STOCK_SELL, stockCode));
            }
            week = segEnd + 1;
        }
        week = Math.min(week, PriceWalk.WEEKS - 1);
        int finalBuyWeek = PriceWalk.argExtreme(path, week, PriceWalk.WEEKS - 1, false);
        events.add(stockEvent(start, finalBuyWeek, SimEvent.Type.STOCK_BUY, stockCode));
        return events;
    }

    private SimEvent stockEvent(LocalDateTime start, int week, SimEvent.Type type, String stockCode) {
        LocalDateTime at = start.plusWeeks(week).plusDays(random.nextInt(7));
        return new SimEvent(at, type, stockCode, null);
    }

    private void handleStockBuy(Long accountId, SimEvent e, Map<String, double[]> stockPaths, LocalDateTime start,
                                 BrokerageDto brokerage, long[] cash, Map<String, RunningStockPosition> positions,
                                 double fractionMin, double fractionMax) {
        double[] path = stockPaths.get(e.stockCode);
        int price = (int) Math.round(path[weekOf(start, e.at)]);
        if (price <= 0 || cash[0] <= 0) {
            return;
        }
        double positionFraction = fractionMin + random.nextDouble() * (fractionMax - fractionMin);
        long budget = (long) (cash[0] * positionFraction);
        long quantity = budget / price;
        if (quantity <= 0) {
            // 목표 비중으로는 1주도 못 사더라도, 실제로 살 여유가 있으면 최소 1주는 매수한다
            // (소액 계좌가 고가 종목을 배정받으면 통째로 거래가 안 생기는 걸 방지)
            if (cash[0] >= price) {
                quantity = 1;
            } else {
                return;
            }
        }
        long amount = (long) price * quantity;
        int fee = feeOf(brokerage, amount);
        long totalCost = amount + fee;
        if (totalCost > cash[0]) {
            return;
        }
        cash[0] -= totalCost;
        positions.computeIfAbsent(e.stockCode, k -> new RunningStockPosition()).addBuy(quantity, price, fee);
        tradeMapper.insertHistoricalTrade(tradeOf(accountId, e.stockCode, "BUY", quantity, price, fee, e.at));
    }

    private void handleStockSell(Long accountId, SimEvent e, Map<String, double[]> stockPaths, LocalDateTime start,
                                  BrokerageDto brokerage, long[] cash, Map<String, RunningStockPosition> positions,
                                  double fractionMin, double fractionMax) {
        RunningStockPosition pos = positions.get(e.stockCode);
        if (pos == null || pos.quantity <= 0) {
            return;
        }
        double[] path = stockPaths.get(e.stockCode);
        int price = (int) Math.round(path[weekOf(start, e.at)]);
        double sellFraction = fractionMin + random.nextDouble() * (fractionMax - fractionMin);
        long sellQty = Math.max(1, (long) (pos.quantity * sellFraction));
        sellQty = Math.min(sellQty, pos.quantity);
        long amount = (long) price * sellQty;
        int fee = feeOf(brokerage, amount);
        long proceeds = amount - fee;
        cash[0] += proceeds;
        pos.reduce(sellQty);
        tradeMapper.insertHistoricalTrade(tradeOf(accountId, e.stockCode, "SELL", sellQty, price, fee, e.at));
    }

    private void handleSubscribe(Long accountId, SimEvent e, List<FinancialProductDto> myProducts,
                                  Map<Long, double[]> productPaths, LocalDateTime start, long[] cash,
                                  Map<Long, RunningProductPosition> positions) {
        double[] path = productPaths.get(e.productId);
        BigDecimal nav = BigDecimal.valueOf(path[weekOf(start, e.at)]).setScale(2, RoundingMode.HALF_UP);
        if (nav.signum() <= 0 || cash[0] <= 0) {
            return;
        }
        double positionFraction = 0.05 + random.nextDouble() * 0.15;
        long budget = (long) (cash[0] * positionFraction);
        BigDecimal quantity = BigDecimal.valueOf(budget).divide(nav, 4, RoundingMode.DOWN);
        if (quantity.signum() <= 0) {
            return;
        }
        long amount = nav.multiply(quantity).setScale(0, RoundingMode.HALF_UP).longValueExact();
        if (amount <= 0 || amount > cash[0]) {
            return;
        }
        cash[0] -= amount;
        positions.computeIfAbsent(e.productId, k -> new RunningProductPosition()).addBuy(quantity, nav);
        productTransactionMapper.insertTransaction(
                productTxOf(accountId, e.productId, "SUBSCRIBE", quantity, nav, amount, e.at));
    }

    private void handleRedeem(Long accountId, SimEvent e, Map<Long, double[]> productPaths, LocalDateTime start,
                               long[] cash, Map<Long, RunningProductPosition> positions) {
        RunningProductPosition pos = positions.get(e.productId);
        if (pos == null || pos.quantity.signum() <= 0) {
            return;
        }
        double[] path = productPaths.get(e.productId);
        BigDecimal nav = BigDecimal.valueOf(path[weekOf(start, e.at)]).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fraction = BigDecimal.valueOf(0.3 + random.nextDouble() * 0.5);
        BigDecimal redeemQty = pos.quantity.multiply(fraction).setScale(4, RoundingMode.DOWN);
        if (redeemQty.signum() <= 0 || redeemQty.compareTo(pos.quantity) > 0) {
            redeemQty = pos.quantity;
        }
        long amount = nav.multiply(redeemQty).setScale(0, RoundingMode.HALF_UP).longValueExact();
        cash[0] += amount;
        pos.reduce(redeemQty);
        productTransactionMapper.insertTransaction(
                productTxOf(accountId, e.productId, "REDEEM", redeemQty, nav, amount, e.at));
    }

    private void handleDeposit(Long accountId, SimEvent e, long[] cash) {
        long amount = 100_000L + (long) (random.nextDouble() * 2_000_000L);
        cash[0] += amount;
        String memo = DEPOSIT_MEMOS[random.nextInt(DEPOSIT_MEMOS.length)];
        cashTransactionMapper.insertTransaction(cashTx(accountId, "DEPOSIT", amount, cash[0], memo, e.at));
    }

    private void handleWithdrawal(Long accountId, SimEvent e, long[] cash) {
        long amount = Math.min(cash[0], 50_000L + (long) (random.nextDouble() * 1_000_000L));
        if (amount <= 0) {
            return;
        }
        cash[0] -= amount;
        String memo = WITHDRAW_MEMOS[random.nextInt(WITHDRAW_MEMOS.length)];
        cashTransactionMapper.insertTransaction(cashTx(accountId, "WITHDRAWAL", amount, cash[0], memo, e.at));
    }

    private void finalizeAccount(Long accountId, List<StockDto> myStocks, long[] cash,
                                  Map<String, RunningStockPosition> stockPositions,
                                  Map<Long, RunningProductPosition> productPositions) {
        long totalPurchase = 0;
        long totalCurrentValue = 0;
        long holdingStockQty = 0;
        for (Map.Entry<String, RunningStockPosition> entry : stockPositions.entrySet()) {
            RunningStockPosition pos = entry.getValue();
            if (pos.quantity <= 0) {
                continue;
            }
            StockDto stock = findStock(myStocks, entry.getKey());
            holdingMapper.insertHolding(holdingOf(accountId, entry.getKey(), pos.quantity, pos.avgPrice));
            totalPurchase += (long) pos.avgPrice * pos.quantity;
            totalCurrentValue += (long) stock.getCurrentPrice() * pos.quantity;
            holdingStockQty += pos.quantity;
        }
        for (Map.Entry<Long, RunningProductPosition> entry : productPositions.entrySet()) {
            RunningProductPosition pos = entry.getValue();
            if (pos.quantity.signum() <= 0) {
                continue;
            }
            long purchaseAmount = pos.avgNav.multiply(pos.quantity).setScale(0, RoundingMode.HALF_UP).longValueExact();
            productHoldingMapper.insertHolding(
                    productHoldingOf(accountId, entry.getKey(), pos.quantity, pos.avgNav, purchaseAmount));
        }

        long profitAmount = totalCurrentValue - totalPurchase;
        BigDecimal returnRate = totalPurchase <= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(profitAmount).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalPurchase), 4, RoundingMode.HALF_UP);

        accountMapper.updateStats(accountId, cash[0], returnRate, profitAmount, holdingStockQty);
    }

    // ==================== 헬퍼 ====================

    private int weekOf(LocalDateTime start, LocalDateTime at) {
        long weeks = ChronoUnit.WEEKS.between(start, at);
        return (int) Math.max(0, Math.min(PriceWalk.WEEKS, weeks));
    }

    // 그 주의 시세(PriceWalk)로 보유 종목/상품을 평가한 총자산(현금 포함) - 스냅샷 저장용
    private long computeTotalAssetAtWeek(long cash,
                                          Map<String, RunningStockPosition> stockPositions, Map<String, double[]> stockPaths,
                                          Map<Long, RunningProductPosition> productPositions, Map<Long, double[]> productPaths,
                                          int week) {
        long total = cash;
        for (Map.Entry<String, RunningStockPosition> entry : stockPositions.entrySet()) {
            RunningStockPosition pos = entry.getValue();
            if (pos.quantity <= 0) {
                continue;
            }
            double priceAtWeek = stockPaths.get(entry.getKey())[week];
            total += Math.round(priceAtWeek * pos.quantity);
        }
        for (Map.Entry<Long, RunningProductPosition> entry : productPositions.entrySet()) {
            RunningProductPosition pos = entry.getValue();
            if (pos.quantity.signum() <= 0) {
                continue;
            }
            double navAtWeek = productPaths.get(entry.getKey())[week];
            total += Math.round(navAtWeek * pos.quantity.doubleValue());
        }
        return total;
    }

    private int feeOf(BrokerageDto brokerage, long amount) {
        return BigDecimal.valueOf(amount).multiply(brokerage.getFeeRate())
                .setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private <T> List<T> pickRandom(List<T> source, int count) {
        List<T> copy = new ArrayList<>(source);
        Collections.shuffle(copy, random);
        return copy.subList(0, Math.min(count, copy.size()));
    }

    private StockDto findStock(List<StockDto> stocks, String stockCode) {
        return stocks.stream().filter(s -> s.getStockCode().equals(stockCode)).findFirst().orElseThrow();
    }

    private CashTransactionDto cashTx(Long accountId, String type, long amount, long balanceAfter,
                                       String memo, LocalDateTime at) {
        CashTransactionDto dto = new CashTransactionDto();
        dto.setAccountId(accountId);
        dto.setTransactionType(type);
        dto.setAmount(amount);
        dto.setBalanceAfter(balanceAfter);
        dto.setMemo(memo);
        dto.setTransactionAt(at);
        return dto;
    }

    private TradeDto tradeOf(Long accountId, String stockCode, String type, long quantity, int price, int fee,
                              LocalDateTime at) {
        TradeDto dto = new TradeDto();
        dto.setAccountId(accountId);
        dto.setStockCode(stockCode);
        dto.setTradeType(type);
        dto.setQuantity(quantity);
        dto.setPrice(price);
        dto.setFee(fee);
        dto.setTradeAt(at);
        return dto;
    }

    private ProductTransactionDto productTxOf(Long accountId, Long productId, String type, BigDecimal quantity,
                                               BigDecimal nav, long amount, LocalDateTime at) {
        ProductTransactionDto dto = new ProductTransactionDto();
        dto.setAccountId(accountId);
        dto.setProductId(productId);
        dto.setTransactionType(type);
        dto.setQuantity(quantity);
        dto.setNav(nav);
        dto.setAmount(amount);
        dto.setTransactionAt(at);
        return dto;
    }

    private HoldingDto holdingOf(Long accountId, String stockCode, long quantity, int avgPrice) {
        HoldingDto dto = new HoldingDto();
        dto.setAccountId(accountId);
        dto.setStockCode(stockCode);
        dto.setQuantity(quantity);
        dto.setAvgPrice(avgPrice);
        return dto;
    }

    private ProductHoldingDto productHoldingOf(Long accountId, Long productId, BigDecimal quantity,
                                                BigDecimal avgNav, long purchaseAmount) {
        ProductHoldingDto dto = new ProductHoldingDto();
        dto.setAccountId(accountId);
        dto.setProductId(productId);
        dto.setQuantity(quantity);
        dto.setAvgNav(avgNav);
        dto.setPurchaseAmount(purchaseAmount);
        dto.setUpdateAt(LocalDateTime.now());
        return dto;
    }

    private void printSummary(int accountCount, List<AccountDto> newDemoAccounts) {
        System.out.println();
        System.out.println("========== 데모 데이터 생성 완료 ==========");
        System.out.println(accountCount + "개 계좌의 1년치 이력을 재생성했습니다.");
        if (!newDemoAccounts.isEmpty()) {
            System.out.println();
            System.out.println("[신규 미연동 데모 계좌 - 회원가입 후 계좌연동(POST /api/accounts/link) 테스트용]");
            for (AccountDto a : newDemoAccounts) {
                System.out.printf("  증권사=%s, 예금주명=%s, 계좌번호=%s%n",
                        a.getBrokerageName(), a.getOwnerName(), a.getAccountNo());
            }
        }
        System.out.println("=============================================");
        System.out.println();
    }

    private static class SimEvent {
        enum Type { STOCK_BUY, STOCK_SELL, PRODUCT_SUBSCRIBE, PRODUCT_REDEEM, CASH_DEPOSIT, CASH_WITHDRAWAL }

        final LocalDateTime at;
        final Type type;
        final String stockCode;
        final Long productId;

        SimEvent(LocalDateTime at, Type type, String stockCode, Long productId) {
            this.at = at;
            this.type = type;
            this.stockCode = stockCode;
            this.productId = productId;
        }
    }

    private static class RunningStockPosition {
        long quantity = 0;
        int avgPrice = 0;

        // 평단가에 매수수수료도 포함 - TradeServiceImpl의 실제 매매 평단가 계산과 동일한 방식
        void addBuy(long qty, int price, int fee) {
            long buyCost = (long) price * qty + fee;
            avgPrice = quantity == 0
                    ? (int) (buyCost / qty)
                    : (int) ((quantity * avgPrice + buyCost) / (quantity + qty));
            quantity += qty;
        }

        void reduce(long qty) {
            quantity -= qty;
        }
    }

    private static class RunningProductPosition {
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal avgNav = BigDecimal.ZERO;

        void addBuy(BigDecimal qty, BigDecimal nav) {
            if (quantity.signum() == 0) {
                avgNav = nav;
            } else {
                avgNav = quantity.multiply(avgNav).add(qty.multiply(nav))
                        .divide(quantity.add(qty), 2, RoundingMode.HALF_UP);
            }
            quantity = quantity.add(qty);
        }

        void reduce(BigDecimal qty) {
            quantity = quantity.subtract(qty);
        }
    }
}
