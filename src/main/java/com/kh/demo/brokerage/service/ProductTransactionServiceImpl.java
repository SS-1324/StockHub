package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.AccountDto;
import com.kh.demo.brokerage.dto.ProductTransactionDto;
import com.kh.demo.brokerage.mapper.AccountMapper;
import com.kh.demo.brokerage.mapper.ProductTransactionMapper;
import com.kh.demo.brokerage.partner.PartnerBrokerageClient;
import com.kh.demo.brokerage.partner.dto.PartnerProductTransactionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductTransactionServiceImpl implements ProductTransactionService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private ProductTransactionMapper productTransactionMapper;

    @Autowired
    private PartnerBrokerageClient partnerBrokerageClient;

    @Override
    public List<ProductTransactionDto> getTransactions(String memberId, Long accountId, LocalDate from, LocalDate to) {
        AccountDto account = accountMapper.selectAccountById(accountId);
        if (account == null) {
            throw new IllegalStateException("존재하지 않는 계좌입니다.");
        }
        if (!memberId.equals(account.getMemberId())) {
            throw new IllegalStateException("본인 계좌만 조회할 수 있습니다.");
        }

        return partnerBrokerageClient.fetchProductTransactions(account.getBrokerageId(), account.getAccountNo(), from, to)
                .getData().stream()
                .map(p -> toInternalDto(accountId, p))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductTransactionDto> getMyTransactions(String memberId) {
        return productTransactionMapper.selectTransactionsByMember(memberId);
    }

    // 파트너 응답을 내부 DTO로 변환 - transactionId는 파트너측 식별자(partnerTransactionId)와 별개라 비워둔다
    private ProductTransactionDto toInternalDto(Long accountId, PartnerProductTransactionDto p) {
        ProductTransactionDto dto = new ProductTransactionDto();
        dto.setAccountId(accountId);
        dto.setProductId(p.getProductId());
        dto.setProductName(p.getProductName());
        dto.setTransactionType(p.getTransactionType());
        dto.setQuantity(p.getQuantity());
        dto.setNav(p.getNav());
        dto.setAmount(p.getAmount());
        dto.setTransactionAt(p.getSettledAt());
        return dto;
    }
}
