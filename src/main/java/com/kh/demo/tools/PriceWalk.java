package com.kh.demo.tools;

import java.util.Random;

/*
*   PriceWalk : 데모 데이터 생성기 전용 - "그럴듯한 과거 시세"를 근사하는 유틸.
*
*   현재가/현재 기준가에서 거꾸로 랜덤워크를 만들어 "1년 전부터 지금까지 이렇게 움직여왔다"를 흉내낸다.
*   마지막 지점이 항상 지금의 실제 값과 정확히 일치하도록 전체 경로를 비례 스케일한다.
*   종목 하나당 이 경로를 딱 한 번만 만들어 stock_price_history에 남기고 모든 계좌가 공유한다(DemoDataService 참고).
* */
public final class PriceWalk {

    public static final int WEEKS = 52;

    private PriceWalk() {}

    // path[0] = 1년 전, path[WEEKS] = 지금(targetPrice와 정확히 일치)
    public static double[] generate(double targetPrice, Random random, double weeklyVolatility) {
        double[] path = new double[WEEKS + 1];
        path[0] = targetPrice;
        for (int i = 1; i <= WEEKS; i++) {
            double change = 1 + (random.nextDouble() * 2 - 1) * weeklyVolatility;
            path[i] = Math.max(path[i - 1] * change, targetPrice * 0.01); // 0 이하로 안 떨어지게 하한
        }
        double scale = targetPrice / path[WEEKS];
        for (int i = 0; i <= WEEKS; i++) {
            path[i] *= scale;
        }
        return path;
    }

    // 성과 편향에 맞춰 "매수 시점(주)"을 고른다 - GOOD은 쌀 때, BAD는 비쌀 때 위주로 매수하게 되어
    // 결과적으로 평단가가 현재가보다 낮아지거나(수익) 높아지도록(손실) 유도한다.
    public static int pickBuyWeek(double[] path, Random random, int biasDirection) {
        // biasDirection: -1(싼 시점 선호) / 0(무작위) / +1(비싼 시점 선호)
        if (biasDirection == 0) {
            return random.nextInt(WEEKS);
        }
        // 후보 2개 중 편향에 맞는 쪽을 채택 (완전 정렬보다 자연스러운 분산을 위해)
        int a = random.nextInt(WEEKS);
        int b = random.nextInt(WEEKS);
        boolean aIsCheaper = path[a] <= path[b];
        if (biasDirection < 0) {
            return aIsCheaper ? a : b;
        } else {
            return aIsCheaper ? b : a;
        }
    }

    // [from, to] 구간 안에서 가장 싸거나(findMax=false) 가장 비싼(findMax=true) 주를 찾는다
    // - "메가 계좌"의 구간별 왕복매매(저점 매수/고점 매도)에 사용
    public static int argExtreme(double[] path, int from, int to, boolean findMax) {
        int best = from;
        for (int w = from; w <= to; w++) {
            if (findMax ? path[w] > path[best] : path[w] < path[best]) {
                best = w;
            }
        }
        return best;
    }
}
