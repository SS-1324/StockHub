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

    public DashboardController(MyStockService myStockService,
                                MyProductService myProductService,
                                TradeService tradeService,
                                ProductTransactionService productTransactionService,
                                CashTransactionService cashTransactionService,
                                RealizedProfitService realizedProfitService,
                                AccountService accountService,
                                BrokerageService brokerageService,
                                GoalService goalService,
                                MemberService memberService) {
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
    }

    @GetMapping("/member/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String memberId = SessionUtil.requireLoginMemberId(session);

        MyStockSummaryDto stockSummary = myStockService.getMyStockSummary(memberId);
        MyProductSummaryDto productSummary = myProductService.getMyProductSummary(memberId);
        List<GoalDto> activeGoals = goalService.getMyActiveGoals(memberId);
        boolean hasGoalHistory = !goalService.getMyGoalHistory(memberId).isEmpty();

        // 총 자산 = 현금 잔고 + 주식 평가금액(매입금액+평가손익) + 상품 평가금액
        long stockCurrentValue = (stockSummary.getTotalPurchaseAmount() == null ? 0L : stockSummary.getTotalPurchaseAmount())
                + (stockSummary.getProfitAmount() == null ? 0L : stockSummary.getProfitAmount());
        long totalAsset = (stockSummary.getCurrentBalance() == null ? 0L : stockSummary.getCurrentBalance())
                + stockCurrentValue
                + (productSummary.getTotalCurrentValue() == null ? 0L : productSummary.getTotalCurrentValue());

        // 총손익 = 총자산 - 총투자원금 (실현손익까지 자동으로 포함되는, 실제 증권사와 동일한 정의)
        PeriodProfitDto periodProfit = realizedProfitService.getMyPeriodProfit(memberId, totalAsset);
        long totalProfit = periodProfit.getAll();
        BigDecimal totalReturnRate = periodProfit.getTotalPrincipal() <= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalProfit * 100)
                        .divide(BigDecimal.valueOf(periodProfit.getTotalPrincipal()), 2, RoundingMode.HALF_UP);

        model.addAttribute("member", memberService.getMemberProfile(memberId));
        model.addAttribute("myAccounts", accountService.getMyAccounts(memberId));
        model.addAttribute("brokerages", brokerageService.getAllBrokerages());
        model.addAttribute("stockSummary", stockSummary);
        model.addAttribute("productSummary", productSummary);
        model.addAttribute("timeline", buildTimeline(memberId));
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
        } catch (IllegalArgumentException e) {
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
        model.addAttribute("member", memberService.getMemberProfile(memberId));
        model.addAttribute("realizedProfits", realizedProfitService.getMyRealizedProfits(memberId));
        return "member/tradeHistory";
    }

    // trade/product_transaction/cash_transaction을 한 타임라인으로 합쳐서 최신순 상위 N개만 반환
    private List<TimelineEventDto> buildTimeline(String memberId) {
        List<TimelineEventDto> events = new ArrayList<>();

        for (TradeDto t : tradeService.getMyTrades(memberId)) {
            boolean isBuy = "BUY".equals(t.getTradeType());
            String description = t.getStockName() + " " + t.getQuantity() + "주";
            long amount = (long) t.getPrice() * t.getQuantity();
            events.add(new TimelineEventDto(t.getTradeAt(), isBuy ? "buy" : "sell", isBuy ? "매수" : "매도", description, amount));
        }
        for (ProductTransactionDto t : productTransactionService.getMyTransactions(memberId)) {
            boolean isSubscribe = "SUBSCRIBE".equals(t.getTransactionType());
            String description = t.getProductName() + " " + t.getQuantity() + "좌";
            events.add(new TimelineEventDto(t.getTransactionAt(), isSubscribe ? "subscribe" : "redeem",
                    isSubscribe ? "가입" : "환매", description, t.getAmount()));
        }
        for (CashTransactionDto t : cashTransactionService.getMyTransactions(memberId)) {
            boolean isDeposit = "DEPOSIT".equals(t.getTransactionType());
            events.add(new TimelineEventDto(t.getTransactionAt(), isDeposit ? "deposit" : "withdrawal",
                    isDeposit ? "입금" : "출금", t.getMemo(), t.getAmount()));
        }

        return events.stream()
                .sorted(Comparator.comparing(TimelineEventDto::getOccurredAt).reversed())
                .limit(TIMELINE_LIMIT)
                .toList();
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
