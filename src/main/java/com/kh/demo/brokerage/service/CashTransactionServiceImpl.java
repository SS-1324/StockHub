package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.AccountDto;
import com.kh.demo.brokerage.dto.CashTransactionDto;
import com.kh.demo.brokerage.mapper.AccountMapper;
import com.kh.demo.brokerage.mapper.CashTransactionMapper;
import com.kh.demo.brokerage.partner.PartnerBrokerageClient;
import com.kh.demo.brokerage.partner.dto.PartnerCashTransactionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashTransactionServiceImpl implements CashTransactionService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private CashTransactionMapper cashTransactionMapper;

    @Autowired
    private PartnerBrokerageClient partnerBrokerageClient;

    @Override
    public List<CashTransactionDto> getTransactions(String memberId, Long accountId, LocalDate from, LocalDate to) {
        AccountDto account = accountMapper.selectAccountById(accountId);
        if (account == null) {
            throw new IllegalStateException("존재하지 않는 계좌입니다.");
        }
        if (!memberId.equals(account.getMemberId())) {
            throw new IllegalStateException("본인 계좌만 조회할 수 있습니다.");
        }

        return partnerBrokerageClient.fetchCashTransactions(account.getBrokerageId(), account.getAccountNo(), from, to)
                .getData().stream()
                .map(p -> toInternalDto(accountId, p))
                .collect(Collectors.toList());
    }

    @Override
    public List<CashTransactionDto> getMyTransactions(String memberId) {
        return cashTransactionMapper.selectTransactionsByMember(memberId);
    }

    @Override
    public long getMyNetDeposits(String memberId, java.time.LocalDate since) {
        return cashTransactionMapper.sumNetDeposits(memberId, since);
    }

    @Override
    public long getMyNetDepositsSinceBaseline(String memberId, java.time.LocalDate periodStart) {
        return cashTransactionMapper.sumNetDepositsSinceBaseline(memberId, periodStart);
    }

    // 파트너 응답을 내부 DTO로 변환 - cashTransactionId는 파트너측 식별자(partnerTransactionId)와 별개라 비워둔다
    private CashTransactionDto toInternalDto(Long accountId, PartnerCashTransactionDto p) {
        CashTransactionDto dto = new CashTransactionDto();
        dto.setAccountId(accountId);
        dto.setTransactionType(p.getTransactionType());
        dto.setAmount(p.getAmount());
        dto.setBalanceAfter(p.getBalanceAfter());
        dto.setMemo(p.getMemo());
        dto.setTransactionAt(p.getSettledAt());
        return dto;
    }
}
