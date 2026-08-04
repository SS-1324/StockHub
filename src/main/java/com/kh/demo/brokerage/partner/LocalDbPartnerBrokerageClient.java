package com.kh.demo.brokerage.partner;

import com.kh.demo.brokerage.dto.AccountDto;
import com.kh.demo.brokerage.dto.CashTransactionDto;
import com.kh.demo.brokerage.dto.ProductHoldingDto;
import com.kh.demo.brokerage.dto.ProductTransactionDto;
import com.kh.demo.brokerage.mapper.AccountMapper;
import com.kh.demo.brokerage.mapper.CashTransactionMapper;
import com.kh.demo.brokerage.mapper.ProductHoldingMapper;
import com.kh.demo.brokerage.mapper.ProductTransactionMapper;
import com.kh.demo.brokerage.partner.dto.PartnerApiResponse;
import com.kh.demo.brokerage.partner.dto.PartnerCashTransactionDto;
import com.kh.demo.brokerage.partner.dto.PartnerHoldingDto;
import com.kh.demo.brokerage.partner.dto.PartnerProductTransactionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/*
*   LocalDbPartnerBrokerageClient : PartnerBrokerageClient의 현재 유일한 구현체.
*
*   실제 파트너사와 통신하는 대신 우리 DB(product_holding/product_transaction/cash_transaction)를
*   그대로 읽어서 파트너 API 응답 모양으로 감싸 돌려준다. 이 3개 테이블 자체가 "가상 증권사가
*   이미 보유한 결과 창고"라는 전제이기 때문에, 호출하는 쪽 입장에서는 실제 제휴사 API를
*   부른 것과 동일한 결과를 받는다.
* */

@Component
public class LocalDbPartnerBrokerageClient implements PartnerBrokerageClient {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private ProductHoldingMapper productHoldingMapper;

    @Autowired
    private ProductTransactionMapper productTransactionMapper;

    @Autowired
    private CashTransactionMapper cashTransactionMapper;

    @Override
    public PartnerApiResponse<PartnerHoldingDto> fetchHoldings(Long brokerageId, String partnerAccountNo) {
        Long accountId = resolveAccountId(brokerageId, partnerAccountNo);

        List<PartnerHoldingDto> result = productHoldingMapper.selectHoldingsByAccount(accountId).stream()
                .map(h -> toPartnerDto(h, partnerAccountNo))
                .collect(Collectors.toList());
        return PartnerApiResponse.ok(result);
    }

    @Override
    public PartnerApiResponse<PartnerProductTransactionDto> fetchProductTransactions(
            Long brokerageId, String partnerAccountNo, LocalDate from, LocalDate to) {
        Long accountId = resolveAccountId(brokerageId, partnerAccountNo);

        List<PartnerProductTransactionDto> result =
                productTransactionMapper.selectTransactionsByAccount(accountId, from, to).stream()
                        .map(t -> toPartnerDto(t, partnerAccountNo))
                        .collect(Collectors.toList());
        return PartnerApiResponse.ok(result);
    }

    @Override
    public PartnerApiResponse<PartnerCashTransactionDto> fetchCashTransactions(
            Long brokerageId, String partnerAccountNo, LocalDate from, LocalDate to) {
        Long accountId = resolveAccountId(brokerageId, partnerAccountNo);

        List<PartnerCashTransactionDto> result =
                cashTransactionMapper.selectTransactionsByAccount(accountId, from, to).stream()
                        .map(t -> toPartnerDto(t, partnerAccountNo))
                        .collect(Collectors.toList());
        return PartnerApiResponse.ok(result);
    }

    // 파트너사 계좌번호를 우리 내부 accountId로 되짚는다 - 실제 HTTP 파트너였어도 필요했을 조회
    private Long resolveAccountId(Long brokerageId, String partnerAccountNo) {
        AccountDto account = accountMapper.selectAccountByAccountNo(brokerageId, partnerAccountNo);
        if (account == null) {
            throw new IllegalStateException("파트너사에서 해당 계좌를 찾을 수 없습니다: " + partnerAccountNo);
        }
        return account.getAccountId();
    }

    private PartnerHoldingDto toPartnerDto(ProductHoldingDto h, String partnerAccountNo) {
        return new PartnerHoldingDto(partnerAccountNo, h.getProductId(), h.getProductName(), h.getProductType(),
                h.getQuantity(), h.getAvgNav(), h.getPurchaseAmount(), h.getUpdateAt());
    }

    private PartnerProductTransactionDto toPartnerDto(ProductTransactionDto t, String partnerAccountNo) {
        return new PartnerProductTransactionDto("PTX-" + t.getTransactionId(), partnerAccountNo,
                t.getProductId(), t.getProductName(), t.getTransactionType(),
                t.getQuantity(), t.getNav(), t.getAmount(), t.getTransactionAt());
    }

    private PartnerCashTransactionDto toPartnerDto(CashTransactionDto t, String partnerAccountNo) {
        return new PartnerCashTransactionDto("CTX-" + t.getCashTransactionId(), partnerAccountNo,
                t.getTransactionType(), t.getAmount(), t.getBalanceAfter(), t.getMemo(), t.getTransactionAt());
    }
}
