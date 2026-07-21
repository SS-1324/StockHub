package com.kh.demo.brokerage.controller;

import com.kh.demo.common.SessionConst;
import com.kh.demo.common.dto.ApiResponse;
import com.kh.demo.brokerage.dto.*;
import com.kh.demo.brokerage.service.AccountService;
import com.kh.demo.brokerage.service.BrokerageService;
import com.kh.demo.brokerage.service.TradeService;
import com.kh.demo.member.dto.MemberDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
*   가상 증권사(Virtual Brokerage) API 컨트롤러
*
*   Trello 카드 "API 수준까지만 만들면 됨(화면 구성 필요 없음)"에 맞춰
*   화면(view) 이동 없이 JSON만 응답하는 REST 컨트롤러로 작성함.
*   다른 팀원의 기능(내 거래 정보, 상품 일괄 조회, 랭킹보드 거래 히스토리 등)이
*   그대로 fetch로 호출해서 쓸 수 있게 만드는 것이 목적.
* */

@RestController
@RequestMapping("/api")
public class BrokerageController {

    @Autowired
    private BrokerageService brokerageService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TradeService tradeService;

    // ------------------- 증권사 / 상품 조회 (로그인 불필요, 공개 정보)

    // 가상 증권사 목록
    @GetMapping("/brokerages")
    public ApiResponse<List<BrokerageDto>> getBrokerages() {
        return ApiResponse.success(brokerageService.getAllBrokerages());
    }

    // 전체 상품 일괄 조회 - 주식은 모든 증권사에서 공통으로 거래 가능하므로 증권사별 필터링은 없음
    @GetMapping("/stocks")
    public ApiResponse<List<StockDto>> getAllStocks() {
        return ApiResponse.success(brokerageService.getAllStocks());
    }

    // 종목코드로 상품 단건 조회
    @GetMapping("/stocks/{stockCode}")
    public ApiResponse<StockDto> getStock(@PathVariable String stockCode) {
        StockDto stock = brokerageService.getStock(stockCode);
        if (stock == null) {
            return ApiResponse.fail("존재하지 않는 종목입니다.");
        }
        return ApiResponse.success(stock);
    }

    // ------------------- 계좌 (로그인 필요)

    // 연동할 기존 계좌 없이, 증권사에 완전히 새로운 계좌를 개설
    @PostMapping("/accounts")
    public ApiResponse<AccountDto> openAccount(@RequestParam Long brokerageId, HttpSession session) {
        try {
            MemberDto member = requireLoginMember(session);
            return ApiResponse.success("계좌가 개설되었습니다.",
                    accountService.openAccount(member.getMemberId(), member.getMemberName(), brokerageId));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    // 특정 증권사의 "아직 연동되지 않은" 계좌 목록 -> 회원가입/마이페이지에서 "정보 불러오기" 후보로 보여줄 목록
    @GetMapping("/brokerages/{brokerageId}/unlinked-accounts")
    public ApiResponse<List<AccountDto>> getUnlinkedAccounts(@PathVariable Long brokerageId) {
        return ApiResponse.success(accountService.getUnlinkedAccounts(brokerageId));
    }

    // 계좌번호 + 예금주명으로 본인확인 후, 증권사에 미리 있던 계좌(+거래이력)를 내 계좌로 연동
    @PostMapping("/accounts/link")
    public ApiResponse<AccountDto> linkAccount(@RequestBody AccountLinkRequestDto request, HttpSession session) {
        try {
            String memberId = requireLoginMemberId(session);
            return ApiResponse.success("계좌가 연동되었습니다.", accountService.linkAccount(memberId, request));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    // 내 가상 계좌 목록 (연동됐거나 새로 개설한 계좌만)
    @GetMapping("/my/accounts")
    public ApiResponse<List<AccountDto>> getMyAccounts(HttpSession session) {
        try {
            String memberId = requireLoginMemberId(session);
            return ApiResponse.success(accountService.getMyAccounts(memberId));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    // 특정 계좌의 보유내역 (본인 계좌만 조회 가능)
    @GetMapping("/accounts/{accountId}/holdings")
    public ApiResponse<List<HoldingDto>> getHoldings(@PathVariable Long accountId, HttpSession session) {
        try {
            String memberId = requireLoginMemberId(session);
            return ApiResponse.success(accountService.getHoldings(memberId, accountId));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    // ------------------- 거래(매수/매도) (로그인 필요)

    // 매수/매도 체결 -> 잔고, 보유내역, 거래이력이 한번에 갱신됨
    @PostMapping("/accounts/{accountId}/trades")
    public ApiResponse<TradeDto> trade(@PathVariable Long accountId,
                                        @RequestBody TradeRequestDto request,
                                        HttpSession session) {
        try {
            String memberId = requireLoginMemberId(session);
            return ApiResponse.success("거래가 체결되었습니다.", tradeService.executeTrade(memberId, accountId, request));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    // 특정 계좌의 거래이력
    @GetMapping("/accounts/{accountId}/trades")
    public ApiResponse<List<TradeDto>> getTradesByAccount(@PathVariable Long accountId, HttpSession session) {
        try {
            String memberId = requireLoginMemberId(session);
            return ApiResponse.success(tradeService.getTradesByAccount(memberId, accountId));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    // 로그인한 회원의 모든 계좌를 통틀어 거래이력 조회 -> "내 거래 정보" 기능이 그대로 호출하면 됨
    @GetMapping("/my/trades")
    public ApiResponse<List<TradeDto>> getMyTrades(HttpSession session) {
        try {
            String memberId = requireLoginMemberId(session);
            return ApiResponse.success(tradeService.getMyTrades(memberId));
        } catch (IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        }
    }

    // ------------------- 공통

    // 세션에서 로그인 회원 정보를 꺼내오고, 로그인이 안 되어있으면 예외 발생
    private MemberDto requireLoginMember(HttpSession session) {
        Object loginMember = session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (loginMember == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return (MemberDto) loginMember;
    }

    // 세션에서 로그인 회원 아이디만 꺼내오고, 로그인이 안 되어있으면 예외 발생
    private String requireLoginMemberId(HttpSession session) {
        return requireLoginMember(session).getMemberId();
    }
}
