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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/*
*   DashboardController : 헤더의 '내 대시보드' 메뉴에서 사용하는 화면 요청을 처리.
*
*   원래는 '내 주식'(주식 보유내역만) 화면이었는데, 상품(펀드/채권/ELS)·입출금·목표 도달률까지
*   포함하는 통합 자산 대시보드로 확장했다. 증권사가 여럿이어도 한 화면에서 다 보인다는 게
*   StockHub의 핵심 가치라는 판단에 따른 것.
* */
@Controller
public class DashboardController {

    private static final int TIMELINE_LIMIT = 15;

    private final MyStockService myStockService;
    private final MyProductService myProductService;
    private final TradeService tradeService;
    private final ProductTransactionService productTransactionService;
    private final CashTransactionService cashTransactionService;
    private final GoalService goalService;
    private final MemberService memberService;

    public DashboardController(MyStockService myStockService,
                                MyProductService myProductService,
                                TradeService tradeService,
                                ProductTransactionService productTransactionService,
                                CashTransactionService cashTransactionService,
                                GoalService goalService,
                                MemberService memberService) {
        this.myStockService = myStockService;
        this.myProductService = myProductService;
        this.tradeService = tradeService;
        this.productTransactionService = productTransactionService;
        this.cashTransactionService = cashTransactionService;
        this.goalService = goalService;
        this.memberService = memberService;
    }

    @GetMapping("/member/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String memberId = SessionUtil.requireLoginMemberId(session);

        MyStockSummaryDto stockSummary = myStockService.getMyStockSummary(memberId);
        MyProductSummaryDto productSummary = myProductService.getMyProductSummary(memberId);
        GoalDto goal = goalService.getMyGoal(memberId);

        model.addAttribute("member", memberService.getMemberProfile(memberId));
        model.addAttribute("stockSummary", stockSummary);
        model.addAttribute("productSummary", productSummary);
        model.addAttribute("timeline", buildTimeline(memberId));
        model.addAttribute("goal", goal);
        model.addAttribute("goalProgress", computeGoalProgress(goal, stockSummary, productSummary));

        // 총 자산 = 현금 잔고 + 주식 평가금액(매입금액+평가손익) + 상품 평가금액
        long stockCurrentValue = (stockSummary.getTotalPurchaseAmount() == null ? 0L : stockSummary.getTotalPurchaseAmount())
                + (stockSummary.getProfitAmount() == null ? 0L : stockSummary.getProfitAmount());
        long totalAsset = (stockSummary.getCurrentBalance() == null ? 0L : stockSummary.getCurrentBalance())
                + stockCurrentValue
                + (productSummary.getTotalCurrentValue() == null ? 0L : productSummary.getTotalCurrentValue());
        model.addAttribute("totalAsset", totalAsset);
        model.addAttribute("totalProfit",
                (stockSummary.getProfitAmount() == null ? 0L : stockSummary.getProfitAmount())
                        + (productSummary.getProfitAmount() == null ? 0L : productSummary.getProfitAmount()));

        return "member/dashboard";
    }

    @PostMapping("/member/dashboard/goal")
    public String setGoal(@RequestParam String goalType,
                           @RequestParam String title,
                           @RequestParam BigDecimal targetValue,
                           HttpSession session,
                           RedirectAttributes ra) {
        String memberId = SessionUtil.requireLoginMemberId(session);
        try {
            goalService.setGoal(memberId, goalType, title, targetValue);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("goalError", e.getMessage());
        }
        return "redirect:/member/dashboard";
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

    // 목표 대비 도달률(%) 계산 - 0~100 사이로 잘라서 프로그레스 바에 바로 쓸 수 있게 한다
    private int computeGoalProgress(GoalDto goal, MyStockSummaryDto stockSummary, MyProductSummaryDto productSummary) {
        if (goal == null) {
            return 0;
        }
        long combinedProfit = (stockSummary.getProfitAmount() == null ? 0L : stockSummary.getProfitAmount())
                + (productSummary.getProfitAmount() == null ? 0L : productSummary.getProfitAmount());

        BigDecimal current;
        if ("PROFIT_AMOUNT".equals(goal.getGoalType())) {
            current = BigDecimal.valueOf(combinedProfit);
        } else {
            long combinedPurchase = (stockSummary.getTotalPurchaseAmount() == null ? 0L : stockSummary.getTotalPurchaseAmount())
                    + (productSummary.getTotalPurchaseAmount() == null ? 0L : productSummary.getTotalPurchaseAmount());
            current = combinedPurchase <= 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(combinedProfit * 100).divide(BigDecimal.valueOf(combinedPurchase), 2, java.math.RoundingMode.HALF_UP);
        }

        if (goal.getTargetValue() == null || goal.getTargetValue().signum() <= 0) {
            return 0;
        }
        int percent = current.multiply(BigDecimal.valueOf(100))
                .divide(goal.getTargetValue(), 0, java.math.RoundingMode.HALF_UP)
                .intValue();
        return Math.max(0, Math.min(100, percent));
    }
}
