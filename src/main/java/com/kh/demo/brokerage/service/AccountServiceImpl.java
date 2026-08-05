package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.AccountDto;
import com.kh.demo.brokerage.dto.AccountLinkRequestDto;
import com.kh.demo.brokerage.dto.HoldingDto;
import com.kh.demo.brokerage.mapper.AccountMapper;
import com.kh.demo.brokerage.mapper.HoldingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private HoldingMapper holdingMapper;

    @Override
    @Transactional
    public AccountDto openAccount(String memberId, String memberName, Long brokerageId) {
        // 이미 해당 증권사에 연동된 계좌가 있는지 확인 (account 테이블의 uq_member_broker 유니크 제약과 동일한 의미)
        boolean alreadyExists = accountMapper.selectAccountsByMember(memberId).stream()
                .anyMatch(account -> account.getBrokerageId().equals(brokerageId));
        if (alreadyExists) {
            throw new IllegalStateException("이미 해당 증권사에 연동된 계좌가 있습니다.");
        }

        AccountDto accountDto = new AccountDto();
        // 증권사측 계좌번호는 사이트에서 새로 발급해주는 형태로 임의 생성
        accountDto.setAccountNo("NEW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        accountDto.setOwnerName(memberName);
        accountDto.setBrokerageId(brokerageId);
        accountMapper.insertAccount(accountDto);

        // account(외부/파트너 소유)를 만든 직후, 개설한 회원과 즉시 연동(account_link, 우리 소유)까지 처리
        accountMapper.linkAccount(accountDto.getAccountId(), memberId);

        // insert 시 채워지지 않은 값(잔고 기본값, 증권사명 등)을 다시 조회해서 완전한 형태로 반환
        return accountMapper.selectAccountById(accountDto.getAccountId());
    }

    @Override
    public List<AccountDto> getMyAccounts(String memberId) {
        return accountMapper.selectAccountsByMember(memberId);
    }

    @Override
    public List<AccountDto> getUnlinkedAccounts(Long brokerageId) {
        return accountMapper.selectUnlinkedAccountsByBrokerage(brokerageId);
    }

    @Override
    public AccountDto linkAccount(String memberId, AccountLinkRequestDto request) {
        // 이미 해당 증권사에 연동된 계좌가 있는지 확인
        boolean alreadyExists = accountMapper.selectAccountsByMember(memberId).stream()
                .anyMatch(account -> account.getBrokerageId().equals(request.getBrokerageId()));
        if (alreadyExists) {
            throw new IllegalStateException("이미 해당 증권사에 연동된 계좌가 있습니다.");
        }

        // 계좌번호 + 예금주명이 일치하는 "미연동" 계좌를 찾는다 (본인확인)
        AccountDto target = accountMapper.selectUnlinkedAccount(
                request.getBrokerageId(), request.getAccountNo(), request.getOwnerName());
        if (target == null) {
            throw new IllegalStateException("일치하는 계좌를 찾을 수 없습니다. 계좌번호와 예금주명을 확인해주세요.");
        }

        accountMapper.linkAccount(target.getAccountId(), memberId);

        // 연동 결과(과거 거래이력이 이미 딸려있는 상태)를 다시 조회해서 반환
        return accountMapper.selectAccountById(target.getAccountId());
    }

    @Override
    public List<HoldingDto> getHoldings(String memberId, Long accountId) {
        AccountDto account = accountMapper.selectAccountById(accountId);
        if (account == null) {
            throw new IllegalStateException("존재하지 않는 계좌입니다.");
        }
        if (!memberId.equals(account.getMemberId())) {
            throw new IllegalStateException("본인 계좌만 조회할 수 있습니다.");
        }
        return holdingMapper.selectHoldingsByAccount(accountId);
    }
}
