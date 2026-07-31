package com.kh.demo.hub.service;


import com.kh.demo.hub.dto.TossTokenResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Service
public class TossAuthServiceImpl implements TossAuthService {

    private final RestClient authClient;
    private final String clientId;
    private final String clientSecret;

    // 캐시된 토큰 상태 (동시성 대비 synchronized 로 보호). 메모리에만 저장되므로
    // 서버를 재시작하면 초기화되고, 다음 호출 때 새로 발급받음
    private volatile String cachedToken;
    private volatile Instant expiresAt = Instant.EPOCH;

    public TossAuthServiceImpl(@Value("${toss.api.oauth-url}") String oauthUrl,
                               @Value("${toss.api.client-id}") String clientId,
                               @Value("${toss.api.client-secret}") String clientSecret) {
        this.authClient = RestClient.builder().baseUrl(oauthUrl).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public synchronized String getAccessToken() {
        // 만료 60초 전을 미리 갱신 시점으로 잡아 경계값 이슈 방지
        if (cachedToken == null || Instant.now().isAfter(expiresAt.minusSeconds(60))) {
            refreshToken();
        }
        return cachedToken;
    }

    private void refreshToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        TossTokenResponseDto response = authClient.post()
                .uri("")
                .body(form)
                .retrieve()
                .body(TossTokenResponseDto.class);

        this.cachedToken = response.access_token();
        this.expiresAt = Instant.now().plusSeconds(response.expires_in());
    }
}