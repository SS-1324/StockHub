package com.kh.demo.brokerage.mapper;

import com.kh.demo.brokerage.dto.MyProductHoldingAccountDto;
import com.kh.demo.brokerage.dto.MyProductHoldingDto;
import com.kh.demo.brokerage.dto.ProductHoldingDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductHoldingMapper {

    // 특정 계좌의 금융상품 보유내역
    List<ProductHoldingDto> selectHoldingsByAccount(Long accountId);

    // 로그인 회원의 모든 계좌를 상품별로 합산한 보유 현황 (대시보드용)
    List<MyProductHoldingDto> selectPortfolioHoldings(String memberId);

    // 위와 같은 대상이지만 상품으로 합치지 않고 증권사(계좌)별로 한 줄씩 - 상품이 여러 증권사에
    // 나뉘어 있을 때 펼쳐 보여주는 용도(합산 결과에 덧붙여 쓴다)
    List<MyProductHoldingAccountDto> selectPortfolioHoldingsByAccount(String memberId);

    // 보유내역 등록 (데모 데이터 생성기 전용)
    int insertHolding(ProductHoldingDto productHoldingDto);

    // 계좌의 상품 보유내역을 전부 삭제 (데모 데이터 생성기가 재생성 전 초기화할 때 사용)
    int deleteHoldingsByAccount(Long accountId);
}
