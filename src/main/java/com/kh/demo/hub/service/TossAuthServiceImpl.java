package com.kh.demo.hub.service;


import com.kh.demo.hub.dto.TossTokenResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;

@Service
public class TossAuthServiceImpl implements TossAuthService {

    // 서버 재시작 후에도 토큰을 재사용할 수 있게 로컬 파일에 캐시 (target/ 하위라 git에는 안 올라감)
    private static final Path TOKEN_CACHE_FILE = Path.of("target", "toss-token.cache");

    private final RestClient authClient;
    private final String clientId;
    private final String clientSecret;

    // 캐시된 토큰 상태 (동시성 대비 synchronized 로 보호)
    private volatile String cachedToken;
    private volatile Instant expiresAt = Instant.EPOCH;

    public TossAuthServiceImpl(@Value("${toss.api.oauth-url}") String oauthUrl,
                               @Value("${toss.api.client-id}") String clientId,
                               @Value("${toss.api.client-secret}") String clientSecret) {
        this.authClient = RestClient.builder().baseUrl(oauthUrl).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        loadFromDisk();
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
        saveToDisk();
    }

    // 서버가 막 떴을 때, 캐시 파일에 아직 유효한 토큰이 남아있으면 재사용 (재발급 생략)
    private void loadFromDisk() {
        if (!Files.exists(TOKEN_CACHE_FILE)) {
            return;
        }
        try {
            Properties props = new Properties();
            try (var in = Files.newInputStream(TOKEN_CACHE_FILE)) {
                props.load(in);
            }
            String token = props.getProperty("access_token");
            String expiresAtEpoch = props.getProperty("expires_at");
            if (token == null || expiresAtEpoch == null) {
                return;
            }
            Instant loadedExpiresAt = Instant.ofEpochSecond(Long.parseLong(expiresAtEpoch));
            if (Instant.now().isBefore(loadedExpiresAt.minusSeconds(60))) {
                this.cachedToken = token;
                this.expiresAt = loadedExpiresAt;
            }
        } catch (IOException | NumberFormatException e) {
            // 캐시 로딩 실패는 무시하고 다음 호출 때 새로 발급받음
        }
    }

    private void saveToDisk() {
        try {
            Files.createDirectories(TOKEN_CACHE_FILE.getParent());
            Properties props = new Properties();
            props.setProperty("access_token", cachedToken);
            props.setProperty("expires_at", String.valueOf(expiresAt.getEpochSecond()));
            try (var out = Files.newOutputStream(TOKEN_CACHE_FILE)) {
                props.store(out, "토스 OAuth 토큰 캐시 - 서버 재시작 시 재사용 (git에는 커밋되지 않음)");
            }
        } catch (IOException e) {
            // 캐시 저장 실패해도 다음 요청 때 다시 시도하면 되므로 무시
        }
    }
}