package com.kh.demo.ranking.service;

import com.kh.demo.brokerage.dto.RealizedProfitDto;
import com.kh.demo.brokerage.service.RealizedProfitService;
import com.kh.demo.ranking.dto.RankingDto;
import com.kh.demo.ranking.mapper.RankingMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class RankingServiceImpl implements RankingService {

    private final RankingMapper rankingMapper;
    private final RealizedProfitService realizedProfitService;

    public RankingServiceImpl(
            RankingMapper rankingMapper,
            RealizedProfitService realizedProfitService
    ) {
        this.rankingMapper = rankingMapper;
        this.realizedProfitService = realizedProfitService;
    }

    @Override
    public List<RankingDto> getRankingBoard(
            boolean includePrivateDetails,
            boolean sortByProfit
    ) {

        /*
         * 랭킹에는 투자정보 공개를 허용한 일반 회원만 들어온다.
         *
         * includePrivateDetails는 기존 Controller/Service 구조와의
         * 호환성을 위해 남겨두지만,
         * 현재 랭킹에서는 비공개 회원을 절대 포함하지 않는다.
         */
        List<RankingDto> rankingList =
                rankingMapper.selectPublicRankingMembers();

        /*
         * 회원별 실현손익을 계산한다.
         *
         * 중요한 점:
         * 현재 들고 있는 주식의 평가손익은 사용하지 않는다.
         *
         * RealizedProfitService가 만들어 놓은
         * '매도 완료 거래'만 이용한다.
         */
        for (RankingDto ranking : rankingList) {
            calculateRealizedPerformance(ranking);
        }

        /*
         * 왼쪽 보드:
         * sortByProfit == false
         * → 누적 실현수익률 높은 순
         *
         * 오른쪽 보드:
         * sortByProfit == true
         * → 누적 실현손익 높은 순
         */
        if (sortByProfit) {

            rankingList.sort(
                    Comparator
                            .comparing(
                                    RankingDto::getProfit,
                                    Comparator.nullsFirst(
                                            Comparator.naturalOrder()
                                    )
                            )
                            .reversed()
                            .thenComparing(RankingDto::getMemberId)
            );

        } else {

            rankingList.sort(
                    Comparator
                            .comparing(
                                    RankingDto::getReturnRate,
                                    Comparator.nullsFirst(
                                            Comparator.naturalOrder()
                                    )
                            )
                            .reversed()
                            .thenComparing(RankingDto::getMemberId)
            );
        }

        /*
         * Java에서 정렬했기 때문에
         * 순위도 여기서 다시 1, 2, 3... 부여한다.
         */
        for (int i = 0; i < rankingList.size(); i++) {
            rankingList.get(i).setRankPosition(i + 1);
        }

        return rankingList;
    }

    /*
     * 헤더의 금/은/동 프레임은
     * '실현수익률 랭킹' 기준 1~3위만 표시한다.
     */
    @Override
    public Integer getHeaderRankPosition(String memberId) {

        if (memberId == null || memberId.isBlank()) {
            return null;
        }

        List<RankingDto> rankingList =
                getRankingBoard(false, false);

        return rankingList.stream()
                .filter(ranking ->
                        memberId.equals(ranking.getMemberId())
                )
                .map(RankingDto::getRankPosition)
                .filter(rank ->
                        rank != null && rank <= 3
                )
                .findFirst()
                .orElse(null);
    }

    /*
     * 프로필 팝업에서 사용하는 순위.
     *
     * 사용자가 수익률 보드에서 프로필을 눌렀다면 수익률 순위,
     * 수익금 보드에서 눌렀다면 수익금 순위를 반환한다.
     */
    @Override
    public Integer getProfileRankPosition(
            String memberId,
            boolean sortByProfit
    ) {

        if (memberId == null || memberId.isBlank()) {
            return null;
        }

        List<RankingDto> rankingList =
                getRankingBoard(false, sortByProfit);

        return rankingList.stream()
                .filter(ranking ->
                        memberId.equals(ranking.getMemberId())
                )
                .map(RankingDto::getRankPosition)
                .filter(rank ->
                        rank != null && rank <= 3
                )
                .findFirst()
                .orElse(null);
    }

    /*
     * 프로필 팝업에서 보여줄
     * 해당 회원의 실현수익률 / 실현손익.
     */
    @Override
    public RankingDto getProfileInvestmentSummary(
            String memberId
    ) {

        RankingDto ranking =
                rankingMapper.selectPublicRankingMember(memberId);

        if (ranking == null) {
            return null;
        }

        calculateRealizedPerformance(ranking);

        return ranking;
    }

    /*
     * 한 회원의 '주식 매도 확정분'만 계산한다.
     *
     * RealizedProfitServiceImpl에서 이미:
     *
     * 매수 수수료 → 평단가에 포함
     * 매도 수수료 → 실현손익에서 차감
     *
     * 하고 있으므로 여기서는 그 결과를 재사용한다.
     */
    private void calculateRealizedPerformance(
            RankingDto ranking
    ) {

        List<RealizedProfitDto> realizedProfits =
                realizedProfitService.getMyRealizedProfits(
                        ranking.getMemberId()
                );

        long totalProfit = 0L;

        /*
         * 누적 실현수익률의 분모.
         *
         * 이미 매도한 물량의
         * 매입원가를 모두 합산한다.
         */
        BigDecimal totalRealizedPurchaseAmount =
                BigDecimal.ZERO;

        for (RealizedProfitDto profit : realizedProfits) {

            /*
             * 금융상품 환매는 제외.
             * 주식 매도 확정분만 랭킹에 반영한다.
             */
            if (!"STOCK".equals(profit.getItemType())) {
                continue;
            }

            totalProfit += profit.getProfitAmount();

            /*
             * 해당 매도 물량의 원가
             *
             * buyPrice에는 RealizedProfitServiceImpl에서
             * 매수 수수료가 반영된 평단가가 들어가 있다.
             */
            BigDecimal purchaseAmount =
                    profit.getBuyPrice()
                            .multiply(profit.getQuantity());

            totalRealizedPurchaseAmount =
                    totalRealizedPurchaseAmount.add(
                            purchaseAmount
                    );
        }

        /*
         * 누적 실현수익률
         *
         * 누적 실현손익
         * ---------------- × 100
         * 매도된 물량의 누적 매입원가
         */
        BigDecimal totalReturnRate;

        if (totalRealizedPurchaseAmount.signum() == 0) {

            totalReturnRate = BigDecimal.ZERO;

        } else {

            totalReturnRate =
                    BigDecimal.valueOf(totalProfit)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(
                                    totalRealizedPurchaseAmount,
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

        ranking.setProfit(totalProfit);
        ranking.setReturnRate(totalReturnRate);

        /*
         * 기존 DTO 필드라 남겨둔다.
         * 이제 랭킹은 현재 보유수량 기준이 아니므로
         * 사용하지 않는다.
         */
        ranking.setHoldingQuantity(0L);
    }
}