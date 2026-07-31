package com.kh.demo.hub.service;

import com.kh.demo.hub.dto.TossCandleResponseDto;

import java.util.List;

public interface TossAuthService {
    // 항상 "유효한" access token을 반환 (필요시 내부에서 자동 재발급)
    String getAccessToken();
}
