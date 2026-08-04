package com.kh.demo.brokerage.partner;

import com.kh.demo.brokerage.partner.dto.PartnerApiResponse;
import com.kh.demo.brokerage.partner.dto.PartnerCashTransactionDto;
import com.kh.demo.brokerage.partner.dto.PartnerHoldingDto;
import com.kh.demo.brokerage.partner.dto.PartnerProductTransactionDto;

import java.time.LocalDate;

/*
*   PartnerBrokerageClient : "진짜 제휴 증권사 API였다면 이런 계약이었을 것"을 표현한 인터페이스.
*
*   지금은 LocalDbPartnerBrokerageClient 하나뿐이고, 이 구현체는 실제로 우리 DB
*   (product_holding/product_transaction/cash_transaction)를 그대로 읽는다.
*   나중에 진짜 제휴가 생기면 이 인터페이스를 구현하는 HTTP 클라이언트로 갈아끼우면 되고,
*   이 인터페이스를 호출하는 ProductHoldingService 등은 전혀 바뀌지 않는다.
* */

public interface PartnerBrokerageClient {

    // 계좌의 상품 보유내역 스냅샷
    PartnerApiResponse<PartnerHoldingDto> fetchHoldings(Long brokerageId, String partnerAccountNo);

    // 계좌의 상품 가입/환매 이력 (from/to는 선택 필터)
    PartnerApiResponse<PartnerProductTransactionDto> fetchProductTransactions(
            Long brokerageId, String partnerAccountNo, LocalDate from, LocalDate to);

    // 계좌의 입출금 이력 (from/to는 선택 필터)
    PartnerApiResponse<PartnerCashTransactionDto> fetchCashTransactions(
            Long brokerageId, String partnerAccountNo, LocalDate from, LocalDate to);
}
