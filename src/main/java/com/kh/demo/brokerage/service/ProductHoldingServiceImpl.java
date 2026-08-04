package com.kh.demo.brokerage.service;

import com.kh.demo.brokerage.dto.AccountDto;
import com.kh.demo.brokerage.dto.ProductHoldingDto;
import com.kh.demo.brokerage.mapper.AccountMapper;
import com.kh.demo.brokerage.partner.PartnerBrokerageClient;
import com.kh.demo.brokerage.partner.dto.PartnerHoldingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductHoldingServiceImpl implements ProductHoldingService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private PartnerBrokerageClient partnerBrokerageClient;

    @Override
    public List<ProductHoldingDto> getHoldings(String memberId, Long accountId) {
        AccountDto account = accountMapper.selectAccountById(accountId);
        if (account == null) {
            throw new IllegalStateException("존재하지 않는 계좌입니다.");
        }
        if (!memberId.equals(account.getMemberId())) {
            throw new IllegalStateException("본인 계좌만 조회할 수 있습니다.");
        }

        // 파트너사 API를 호출한다는 전제로, 계좌번호(accountNo) 기준으로 조회
        return partnerBrokerageClient.fetchHoldings(account.getBrokerageId(), account.getAccountNo())
                .getData().stream()
                .map(p -> toInternalDto(accountId, p))
                .collect(Collectors.toList());
    }

    // 파트너 응답을 내부 DTO로 변환 - 파트너사는 우리 내부 PK를 모르므로 productHoldingId는 비워둔다
    private ProductHoldingDto toInternalDto(Long accountId, PartnerHoldingDto p) {
        ProductHoldingDto dto = new ProductHoldingDto();
        dto.setAccountId(accountId);
        dto.setProductId(p.getProductId());
        dto.setProductName(p.getProductName());
        dto.setProductType(p.getProductType());
        dto.setQuantity(p.getQuantity());
        dto.setAvgNav(p.getAvgNav());
        dto.setPurchaseAmount(p.getPurchaseAmount());
        dto.setUpdateAt(p.getAsOf());
        return dto;
    }
}
