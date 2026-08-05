package com.kh.demo.tools;

import com.kh.demo.brokerage.dto.*;
import com.kh.demo.brokerage.mapper.*;
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

    // 시드 고정 -> 재실행해도 같은 결과가 나와서 "손보면서 반복"하기 편함
    private final Random random = new Random(20260805L);

    private enum Activity { QUIET, MODERATE, ACTIVE }
    private enum Performance { GOOD, NEUTRAL, BAD }

    private static final String[] DEPOSIT_MEMOS = {"급여 이체", "용돈 입금", "이체 입금"};
    private static final String[] WITHDRAW_MEMOS = {"생활비 출금", "경조사비 출금", "카드값 출금"};

    @Transactional
    public void generate() {
        seedFinancialProductsIfEmpty();
        List<AccountDto> newDemoAccounts = seedUnlinkedDemoAccountsIfMissing();

        List<AccountDto> targets = accountMapper.selectAllNonAdminAccounts();
        List<StockDto> stocks = stockMapper.selectAllStocks();
        List<FinancialProductDto> products = financialProductMapper.selectProducts(null, null);

        List<Activity> activityPool = pool(Activity.values(), targets.size());
        List<Performance> performancePool = pool(Performance.values(), targets.size());
        Collections.shuffle(activityPool, random);
        Collections.shuffle(performancePool, random);

        for (int i = 0; i < targets.size(); i++) {
            regenerateHistory(targets.get(i), activityPool.get(i), performancePool.get(i), stocks, products);
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

    // ==================== 3. 계좌별 1년 이력 재생성 ====================

    private void regenerateHistory(AccountDto account, Activity activity, Performance performance,
                                    List<StockDto> allStocks, List<FinancialProductDto> allProducts) {
        Long accountId = account.getAccountId();

        // 재실행 안전을 위해 이 계좌의 생성기 대상 데이터를 전부 비우고 다시 채운다
        tradeMapper.deleteTradesByAccount(accountId);
        holdingMapper.deleteHoldingsByAccount(accountId);
        productTransactionMapper.deleteTransactionsByAccount(accountId);
        productHoldingMapper.deleteHoldingsByAccount(accountId);
        cashTransactionMapper.deleteTransactionsByAccount(accountId);

        BrokerageDto brokerage = brokerageMapper.selectBrokerageById(account.getBrokerageId());
        LocalDateTime start = LocalDateTime.now().minusYears(1);

        long principal = Math.max(account.getBalance() == null ? 0L : account.getBalance(), 100_000L);
        long[] cash = {principal};
        cashTransactionMapper.insertTransaction(
                cashTx(accountId, "DEPOSIT", principal, cash[0], "초기 입금", start));

        int stockCount = switch (activity) {
            case QUIET -> 2 + random.nextInt(2);
            case MODERATE -> 3 + random.nextInt(2);
            case ACTIVE -> 4 + random.nextInt(2);
        };
        int tradesPerStock = switch (activity) {
            case QUIET -> 5 + random.nextInt(3);
            case MODERATE -> 7 + random.nextInt(3);
            case ACTIVE -> 9 + random.nextInt(4);
        };
        int bias = switch (performance) {
            case GOOD -> -1;
            case BAD -> 1;
            case NEUTRAL -> 0;
        };

        List<StockDto> myStocks = pickRandom(allStocks, Math.min(stockCount, allStocks.size()));
        Map<String, double[]> stockPaths = new HashMap<>();
        for (StockDto s : myStocks) {
            stockPaths.put(s.getStockCode(), PriceWalk.generate(s.getCurrentPrice(), random, 0.06));
        }

        int productCount = Math.min(random.nextInt(3), allProducts.size()); // 0~2
        List<FinancialProductDto> myProducts = pickRandom(allProducts, productCount);
        Map<Long, double[]> productPaths = new HashMap<>();
        for (FinancialProductDto p : myProducts) {
            productPaths.put(p.getProductId(), PriceWalk.generate(p.getNav().doubleValue(), random, 0.015));
        }

        List<SimEvent> events = buildEvents(start, myStocks, tradesPerStock, stockPaths, bias, myProducts);
        events.sort(Comparator.comparing(e -> e.at));

        Map<String, RunningStockPosition> stockPositions = new HashMap<>();
        Map<Long, RunningProductPosition> productPositions = new HashMap<>();

        for (SimEvent e : events) {
            switch (e.type) {
                case STOCK_BUY -> handleStockBuy(accountId, e, stockPaths, start, brokerage, cash, stockPositions);
                case STOCK_SELL -> handleStockSell(accountId, e, stockPaths, start, brokerage, cash, stockPositions);
                case PRODUCT_SUBSCRIBE -> handleSubscribe(accountId, e, myProducts, productPaths, start, cash, productPositions);
                case PRODUCT_REDEEM -> handleRedeem(accountId, e, productPaths, start, cash, productPositions);
                case CASH_DEPOSIT -> handleDeposit(accountId, e, cash);
                case CASH_WITHDRAWAL -> handleWithdrawal(accountId, e, cash);
            }
        }

        finalizeAccount(accountId, myStocks, cash, stockPositions, productPositions);
    }

    private List<SimEvent> buildEvents(LocalDateTime start, List<StockDto> myStocks, int tradesPerStock,
                                        Map<String, double[]> stockPaths, int bias,
                                        List<FinancialProductDto> myProducts) {
        List<SimEvent> events = new ArrayList<>();
        for (StockDto s : myStocks) {
            double[] path = stockPaths.get(s.getStockCode());
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

    private void handleStockBuy(Long accountId, SimEvent e, Map<String, double[]> stockPaths, LocalDateTime start,
                                 BrokerageDto brokerage, long[] cash, Map<String, RunningStockPosition> positions) {
        double[] path = stockPaths.get(e.stockCode);
        int price = (int) Math.round(path[weekOf(start, e.at)]);
        if (price <= 0 || cash[0] <= 0) {
            return;
        }
        double positionFraction = 0.05 + random.nextDouble() * 0.15; // 잔고의 5~20%
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
        positions.computeIfAbsent(e.stockCode, k -> new RunningStockPosition()).addBuy(quantity, price);
        tradeMapper.insertHistoricalTrade(tradeOf(accountId, e.stockCode, "BUY", quantity, price, fee, e.at));
    }

    private void handleStockSell(Long accountId, SimEvent e, Map<String, double[]> stockPaths, LocalDateTime start,
                                  BrokerageDto brokerage, long[] cash, Map<String, RunningStockPosition> positions) {
        RunningStockPosition pos = positions.get(e.stockCode);
        if (pos == null || pos.quantity <= 0) {
            return;
        }
        double[] path = stockPaths.get(e.stockCode);
        int price = (int) Math.round(path[weekOf(start, e.at)]);
        long sellQty = Math.max(1, (long) (pos.quantity * (0.3 + random.nextDouble() * 0.5)));
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

        void addBuy(long qty, int price) {
            if (quantity == 0) {
                avgPrice = price;
            } else {
                avgPrice = (int) ((quantity * avgPrice + qty * (long) price) / (quantity + qty));
            }
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
