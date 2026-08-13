package com.kh.demo.brokerage.controller;

import com.kh.demo.brokerage.dto.*;
import com.kh.demo.brokerage.service.*;
import com.kh.demo.common.util.SessionUtil;
import com.kh.demo.goal.dto.GoalDto;
import com.kh.demo.goal.service.GoalService;
import com.kh.demo.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*
*   DashboardController : 헤더의 '내 금융거래' 메뉴에서 사용하는 화면 요청을 처리.
*
*   원래는 '내 주식'(주식 보유내역만) 화면이었는데, 상품(펀드/채권/ELS)·입출금·목표 도달률·
*   계좌 연동·매매 손익 내역까지 포함하는 통합 자산 대시보드로 확장했다. 증권사가 여럿이어도
*   한 화면에서 다 보인다는 게 StockHub의 핵심 가치라는 판단에 따른 것.
* */
@Controller
public class DashboardController {

    // 대시보드 최근 활동은 화면에서 15개씩 페이지로 나눠 보여주므로, 페이징할 여지가 있도록 넉넉히 가져온다
    private static final int TIMELINE_LIMIT = 90;

    private final MyStockService myStockService;
    private final MyProductService myProductService;
    private final TradeService tradeService;
    private final ProductTransactionService productTransactionService;
    private final CashTransactionService cashTransactionService;
    private final RealizedProfitService realizedProfitService;
    private final AccountService accountService;
    private final BrokerageService brokerageService;
    private final GoalService goalService;
    private final MemberService memberService;
    private final PortfolioAnalyticsService portfolioAnalyticsService;

    public DashboardController(MyStockService myStockService,
                                MyProductService myProductService,
                                TradeService tradeService,
                                ProductTransactionService productTransactionService,
                                CashTransactionService cashTransactionService,
                                RealizedProfitService realizedProfitService,
                                AccountService accountService,
                                BrokerageService brokerageService,
                                GoalService goalService,
                                MemberService memberService,
                                PortfolioAnalyticsService portfolioAnalyticsService) {
        this.myStockService = myStockService;
        this.myProductService = myProductService;
        this.tradeService = tradeService;
        this.productTransactionService = productTransactionService;
        this.cashTransactionService = cashTransactionService;
        this.realizedProfitService = realizedProfitService;
        this.accountService = accountService;
        this.brokerageService = brokerageService;
        this.goalService = goalService;
        this.memberService = memberService;
        this.portfolioAnalyticsService = portfolioAnalyticsService;
    }

    @GetMapping("/member/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String memberId = SessionUtil.requireLoginMemberId(session);

        MyStockSummaryDto stockSummary = myStockService.getMyStockSummary(memberId);
        MyProductSummaryDto productSummary = myProductService.getMyProductSummary(memberId);
        List<GoalDto> activeGoals = goalService.getMyActiveGoals(memberId);
        boolean hasGoalHistory = !goalService.getMyGoalHistory(memberId).isEmpty();
        List<AccountDto> myAccounts = accountService.getMyAccounts(memberId);

        // 총 자산 = 현금 잔고 + 주식 평가금액(매입금액+평가손익) + 상품 평가금액
        long stockCurrentValue = (stockSummary.getTotalPurchaseAmount() == null ? 0L : stockSummary.getTotalPurchaseAmount())
                + (stockSummary.getProfitAmount() == null ? 0L : stockSummary.getProfitAmount());
        long totalAsset = (stockSummary.getCurrentBalance() == null ? 0L : stockSummary.getCurrentBalance())
                + stockCurrentValue
                + (productSummary.getTotalCurrentValue() == null ? 0L : productSummary.getTotalCurrentValue());

        // 총손익 = 총자산 - 총투자원금 (실현손익까지 자동으로 포함되는, 실제 증권사와 동일한 정의)
        PeriodProfitDto periodProfit = realizedProfitService.getMyPeriodProfit(memberId, totalAsset);
        long totalProfit = periodProfit.getAll();
        BigDecimal totalReturnRate = periodProfit.getAllRate();

        model.addAttribute("member", memberService.getMemberProfile(memberId));
        model.addAttribute("myAccounts", myAccounts);
        model.addAttribute("brokerages", brokerageService.getAllBrokerages());
        model.addAttribute("stockSummary", stockSummary);
        model.addAttribute("productSummary", productSummary);
        model.addAttribute("timeline", buildTimeline(memberId, myAccounts));
        Map<Long, Integer> goalProgress = new HashMap<>();
        Map<Long, Boolean> goalSuccess = new HashMap<>();
        computeGoalProgress(activeGoals, memberId, totalAsset, goalProgress, goalSuccess);

        model.addAttribute("activeGoals", activeGoals);
        model.addAttribute("hasGoalHistory", hasGoalHistory);
        model.addAttribute("goalProgress", goalProgress);
        model.addAttribute("goalSuccess", goalSuccess);
        model.addAttribute("periodProfit", periodProfit);
        model.addAttribute("totalAsset", totalAsset);
        model.addAttribute("totalProfit", totalProfit);
        model.addAttribute("totalReturnRate", totalReturnRate);

        // 증권사 필터: "전체"가 아니라 특정 증권사를 골랐을 때 화면 전체(총자산/총손익/현금잔고/보유주식/
        // 보유상품/최근활동)가 그 증권사 것만으로 다시 그려지도록, 증권사별 수치를 미리 계산해 JSON으로 심어둔다.
        // (평가손익 기준 - "전체" 화면의 총손익처럼 실현손익까지 정교하게 계산하려면 계좌 단위 자산 스냅샷이
        // 새로 필요해서, 필터된 화면에서는 그보다 단순한 평가손익으로 근사한다)
        List<String> myBrokerageNames = myAccounts.stream().map(AccountDto::getBrokerageName).distinct().toList();
        model.addAttribute("myBrokerageNames", myBrokerageNames);
        model.addAttribute("brokerageFilterDataJson", buildBrokerageFilterDataJson(myAccounts, stockSummary, productSummary));
        model.addAttribute("portfolioAnalytics", portfolioAnalyticsService.getMyPortfolioAnalytics(memberId, stockSummary));
        // 포트폴리오 분석 카드(매매 승률/평균 보유기간/최고·최악의 매매)를 클릭했을 때 보여줄 "상세정보"용 -
        // 이미 계산된 요약 수치 뒤에 깔린 실제 매매 내역 전체를 그대로 내려보낸다
        model.addAttribute("realizedProfits", realizedProfitService.getMyRealizedProfits(memberId));

        return "member/dashboard";
    }

    // 계좌번호+예금주명으로 증권사에 본인확인 요청 -> 이미 있던 이력이 그대로 딸려온다
    @PostMapping("/member/dashboard/link-account")
    public String linkAccount(@RequestParam Long brokerageId,
                               @RequestParam String accountNo,
                               @RequestParam String ownerName,
                               HttpSession session,
                               RedirectAttributes ra) {
        String memberId = SessionUtil.requireLoginMemberId(session);
        AccountLinkRequestDto request = new AccountLinkRequestDto();
        request.setBrokerageId(brokerageId);
        request.setAccountNo(accountNo);
        request.setOwnerName(ownerName);
        try {
            accountService.linkAccount(memberId, request);
            ra.addFlashAttribute("linkSuccess", true);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("linkError", e.getMessage());
        }
        return "redirect:/member/dashboard";
    }

    @PostMapping("/member/dashboard/goal")
    public String setGoal(@RequestParam String goalType,
                           @RequestParam String title,
                           @RequestParam BigDecimal targetValue,
                           @RequestParam(required = false) LocalDate targetDate,
                           HttpSession session,
                           RedirectAttributes ra) {
        String memberId = SessionUtil.requireLoginMemberId(session);
        try {
            goalService.setGoal(memberId, goalType, title, targetValue, targetDate);
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("goalError", e.getMessage());
        }
        return "redirect:/member/dashboard";
    }

    @PostMapping("/member/dashboard/goal/cancel")
    public String cancelGoal(@RequestParam Long goalId, HttpSession session, RedirectAttributes ra) {
        String memberId = SessionUtil.requireLoginMemberId(session);
        try {
            goalService.cancelGoal(memberId, goalId);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("goalError", e.getMessage());
        }
        return "redirect:/member/dashboard";
    }

    // 지난 목표(기한+유예기간이 지나 대시보드에서 빠진 것) 모아보기
    @GetMapping("/member/dashboard/goals/history")
    public String goalHistory(HttpSession session, Model model) {
        String memberId = SessionUtil.requireLoginMemberId(session);
        model.addAttribute("member", memberService.getMemberProfile(memberId));
        model.addAttribute("goalHistory", goalService.getMyGoalHistory(memberId));
        return "member/goalHistory";
    }

    // "언제 사서 얼마에 팔아 얼마 벌었나" 매매 손익 내역 전체보기
    @GetMapping("/member/dashboard/history")
    public String tradeHistory(HttpSession session, Model model) {
        String memberId = SessionUtil.requireLoginMemberId(session);
        List<RealizedProfitDto> realizedProfits = realizedProfitService.getMyRealizedProfits(memberId);
        MyStockSummaryDto stockSummary = myStockService.getMyStockSummary(memberId);

        long totalBuyAmount = 0;
        long totalSellAmount = 0;
        long totalProfitAmount = 0;
        for (RealizedProfitDto r : realizedProfits) {
            totalBuyAmount += r.getBuyPrice().multiply(r.getQuantity()).longValue();
            totalSellAmount += r.getSellPrice().multiply(r.getQuantity()).longValue();
            totalProfitAmount += r.getProfitAmount();
        }
        BigDecimal totalProfitRate = totalBuyAmount <= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalProfitAmount * 100).divide(BigDecimal.valueOf(totalBuyAmount), 2, RoundingMode.HALF_UP);

        // 현재 보유 중인 주식의 평가금액 = 매입금액 + 평가손익 (dashboard()의 총자산 계산과 같은 방식)
        long currentHoldingsValue = (stockSummary.getTotalPurchaseAmount() == null ? 0L : stockSummary.getTotalPurchaseAmount())
                + (stockSummary.getProfitAmount() == null ? 0L : stockSummary.getProfitAmount());

        model.addAttribute("member", memberService.getMemberProfile(memberId));
        model.addAttribute("realizedProfits", realizedProfits);
        model.addAttribute("stockSummary", stockSummary);
        model.addAttribute("totalBuyAmount", totalBuyAmount);
        model.addAttribute("totalSellAmount", totalSellAmount);
        model.addAttribute("totalProfitAmount", totalProfitAmount);
        model.addAttribute("totalProfitRate", totalProfitRate);
        model.addAttribute("currentHoldingsValue", currentHoldingsValue);
        return "member/tradeHistory";
    }

    // trade/product_transaction/cash_transaction을 한 타임라인으로 합쳐서 최신순 상위 N개만 반환
    private List<TimelineEventDto> buildTimeline(String memberId, List<AccountDto> myAccounts) {
        Map<Long, String> brokerageByAccount = new HashMap<>();
        for (AccountDto acc : myAccounts) {
            brokerageByAccount.put(acc.getAccountId(), acc.getBrokerageName());
        }

        List<TimelineEventDto> events = new ArrayList<>();

        for (TradeDto t : tradeService.getMyTrades(memberId)) {
            boolean isBuy = "BUY".equals(t.getTradeType());
            String description = t.getStockName() + " " + t.getQuantity() + "주";
            long amount = (long) t.getPrice() * t.getQuantity();
            events.add(new TimelineEventDto(t.getTradeAt(), isBuy ? "buy" : "sell", isBuy ? "매수" : "매도", description, amount,
                    t.getAccountId(), brokerageByAccount.get(t.getAccountId())));
        }
        for (ProductTransactionDto t : productTransactionService.getMyTransactions(memberId)) {
            boolean isSubscribe = "SUBSCRIBE".equals(t.getTransactionType());
            String description = t.getProductName() + " " + t.getQuantity() + "좌";
            events.add(new TimelineEventDto(t.getTransactionAt(), isSubscribe ? "subscribe" : "redeem",
                    isSubscribe ? "가입" : "환매", description, t.getAmount(),
                    t.getAccountId(), brokerageByAccount.get(t.getAccountId())));
        }
        for (CashTransactionDto t : cashTransactionService.getMyTransactions(memberId)) {
            boolean isDeposit = "DEPOSIT".equals(t.getTransactionType());
            events.add(new TimelineEventDto(t.getTransactionAt(), isDeposit ? "deposit" : "withdrawal",
                    isDeposit ? "입금" : "출금", t.getMemo(), t.getAmount(),
                    t.getAccountId(), brokerageByAccount.get(t.getAccountId())));
        }

        return events.stream()
                .sorted(Comparator.comparing(TimelineEventDto::getOccurredAt).reversed())
                .limit(TIMELINE_LIMIT)
                .toList();
    }

    // 증권사 필터가 화면을 다시 그릴 때 쓸 수치(총자산/평가손익/현금잔고)를 "전체"는 빼고
    // 증권사 이름별로만 계산해 JSON 문자열로 반환한다("전체"는 이미 서버에서 계산해 보여주고 있어서 중복 불필요)
    private String buildBrokerageFilterDataJson(List<AccountDto> myAccounts, MyStockSummaryDto stockSummary,
                                                 MyProductSummaryDto productSummary) {
        Map<String, Long> cashByBrokerage = new LinkedHashMap<>();
        Map<String, Long> investedValueByBrokerage = new LinkedHashMap<>();
        Map<String, Long> profitByBrokerage = new LinkedHashMap<>();

        for (AccountDto acc : myAccounts) {
            cashByBrokerage.merge(acc.getBrokerageName(), acc.getBalance() == null ? 0L : acc.getBalance(), Long::sum);
        }
        if (stockSummary.getHoldings() != null) {
            for (MyStockHoldingDto h : stockSummary.getHoldings()) {
                if (h.getAccountBreakdown() == null) {
                    continue;
                }
                for (MyStockHoldingAccountDto row : h.getAccountBreakdown()) {
                    investedValueByBrokerage.merge(row.getBrokerageName(), row.getCurrentValue() == null ? 0L : row.getCurrentValue(), Long::sum);
                    profitByBrokerage.merge(row.getBrokerageName(), row.getProfitAmount() == null ? 0L : row.getProfitAmount(), Long::sum);
                }
            }
        }
        if (productSummary.getHoldings() != null) {
            for (MyProductHoldingDto h : productSummary.getHoldings()) {
                if (h.getAccountBreakdown() == null) {
                    continue;
                }
                for (MyProductHoldingAccountDto row : h.getAccountBreakdown()) {
                    investedValueByBrokerage.merge(row.getBrokerageName(), row.getCurrentValue() == null ? 0L : row.getCurrentValue(), Long::sum);
                    profitByBrokerage.merge(row.getBrokerageName(), row.getProfitAmount() == null ? 0L : row.getProfitAmount(), Long::sum);
                }
            }
        }

        Map<String, Long> totalAssetByBrokerage = new LinkedHashMap<>();
        Map<String, BigDecimal> returnRateByBrokerage = new LinkedHashMap<>();
        for (String name : myAccounts.stream().map(AccountDto::getBrokerageName).distinct().toList()) {
            long cash = cashByBrokerage.getOrDefault(name, 0L);
            long invested = investedValueByBrokerage.getOrDefault(name, 0L);
            long profit = profitByBrokerage.getOrDefault(name, 0L);
            long purchase = invested - profit;
            totalAssetByBrokerage.put(name, cash + invested);
            returnRateByBrokerage.put(name, purchase <= 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(profit * 100).divide(BigDecimal.valueOf(purchase), 2, RoundingMode.HALF_UP));
        }

        StringBuilder json = new StringBuilder("{");
        json.append("\"totalAsset\":").append(toJsonObject(totalAssetByBrokerage)).append(",");
        json.append("\"totalProfit\":").append(toJsonObject(profitByBrokerage)).append(",");
        json.append("\"totalReturnRate\":").append(toJsonObject(returnRateByBrokerage)).append(",");
        json.append("\"currentBalance\":").append(toJsonObject(cashByBrokerage));
        json.append("}");
        return json.toString();
    }

    // 증권사 이름(문자열) -> 숫자 맵 하나를 JSON 오브젝트 문자열로 직접 조립한다
    // (이 프로젝트는 JSP 렌더링 위주라 Jackson 등 JSON 라이브러리가 클래스패스에 없어 손으로 만든다)
    private String toJsonObject(Map<String, ? extends Number> values) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ? extends Number> entry : values.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":").append(entry.getValue());
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // 목표별 대비 도달률(%) 계산 - 0~100 사이로 잘라서 원형 그래프에 바로 쓸 수 있게 한다 (goalId -> 퍼센트/성공여부)
    // 목표를 "세운 시점부터 지금까지"의 성과만 반영해야 한다 - 목표 세우기 전에 이미 벌어놓은 손익까지
    // 그 목표의 진행률로 잡으면 안 되기 때문에, 각 목표의 create_at을 기준일로 삼아 개별 계산한다.
    private void computeGoalProgress(List<GoalDto> goals, String memberId, long totalAsset,
                                      Map<Long, Integer> progressOut, Map<Long, Boolean> successOut) {
        LocalDate today = LocalDate.now();

        for (GoalDto goal : goals) {
            LocalDate goalStart = goal.getCreateAt().toLocalDate();

            // 목표일이 지났으면 그 시점 자산 스냅샷으로 고정한다 - 이후 매수/매도가 더 생겨도
            // 이 목표의 결과는 더 이상 안 움직인다 (목표일 전이면 지금 이 순간의 라이브 총자산 사용)
            boolean isPastDeadline = goal.getTargetDate() != null && !goal.getTargetDate().isAfter(today);
            long assetForProgress = isPastDeadline
                    ? realizedProfitService.getBaselineAsset(memberId, goal.getTargetDate())
                    : totalAsset;
            long profitSinceGoal = realizedProfitService.getProfitSince(memberId, assetForProgress, goalStart);

            BigDecimal current;
            if ("PROFIT_AMOUNT".equals(goal.getGoalType())) {
                current = BigDecimal.valueOf(profitSinceGoal);
            } else {
                long baselineAsset = realizedProfitService.getBaselineAsset(memberId, goalStart);
                current = baselineAsset <= 0
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(profitSinceGoal * 100).divide(BigDecimal.valueOf(baselineAsset), 2, RoundingMode.HALF_UP);
            }
            int rawPercent = (goal.getTargetValue() == null || goal.getTargetValue().signum() <= 0)
                    ? 0
                    : current.multiply(BigDecimal.valueOf(100))
                            .divide(goal.getTargetValue(), 0, RoundingMode.HALF_UP)
                            .intValue();

            // 성공 표시는 목표일이 지나 결과가 확정된 뒤에만 켠다 - 아직 진행 중인데 일시적으로
            // 100%를 넘긴 걸 축하해버리면 나중에 다시 미달로 내려갈 때 혼란스럽다
            boolean success = isPastDeadline && rawPercent >= 100;
            progressOut.put(goal.getGoalId(), Math.max(0, Math.min(100, rawPercent)));
            successOut.put(goal.getGoalId(), success);
        }
    }
}
