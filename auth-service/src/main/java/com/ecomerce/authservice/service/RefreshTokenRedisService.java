package com.ecomerce.authservice.service;

import com.ecomerce.authservice.dto.request.auth.SessionMetaRequest;
import com.ecomerce.authservice.dto.response.auth.SessionMetaResponseDto;

import java.time.Duration;
import java.util.List;

public interface RefreshTokenRedisService {
    void saveRefreshToken(String token, String userId, SessionMetaRequest sessionMetaRequest, Duration expire);

    boolean validateToken(String token,String userId);

    void deleteRefreshToken(String token,String userId);

    void deleteRefreshToken(String key);

    List<SessionMetaResponseDto> getAllSessionMeta(String userId, String currentRefreshToken);
}
