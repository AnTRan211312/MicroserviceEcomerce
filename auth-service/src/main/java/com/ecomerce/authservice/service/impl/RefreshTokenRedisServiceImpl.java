package com.ecomerce.authservice.service.impl;

import com.ecomerce.authservice.dto.request.auth.SessionMetaRequest;
import com.ecomerce.authservice.dto.response.auth.SessionMetaResponseDto;
import com.ecomerce.authservice.model.SessionMeta;
import com.ecomerce.authservice.service.RefreshTokenRedisService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisServiceImpl implements RefreshTokenRedisService {

    private final RedisTemplate<String, SessionMeta> redisSessionMeta;
    private String buildKey(String token,String userId){
        return "auth::refresh_token" + userId + ":" + DigestUtils.sha256Hex(token);
    }
    @Override
    public void saveRefreshToken(String token, String userId, SessionMetaRequest sessionMetaRequest, Duration expire) {
         String sessionId = buildKey(token,userId);
         SessionMeta sessionMeta = new SessionMeta(
                 sessionId,
                 sessionMetaRequest.getDeviceName(),
                 sessionMetaRequest.getDeviceType(),
                 sessionMetaRequest.getUserAgent(),
                 Instant.now()
         );
         redisSessionMeta.opsForValue().set(sessionId, sessionMeta, expire);
    }

    @Override
    public boolean validateToken(String token, String userId) {

        return redisSessionMeta.hasKey(buildKey(token,userId));
    }

    @Override
    public void deleteRefreshToken(String token, String userId) {
        redisSessionMeta.delete(buildKey(token,userId));

    }

    @Override
    public void deleteRefreshToken(String key) {
        redisSessionMeta.delete(key);
    }


    @Override
    public List<SessionMetaResponseDto> getAllSessionMeta(String userId, String currentRefreshToken) {
        String keyPartten = "auth::refresh_token" + userId + ":";
        Set<String> keys = redisSessionMeta.keys(keyPartten);
        if(keys == null || keys.isEmpty()) return Collections.emptyList();
        String currentTokenHash = DigestUtils.sha256Hex(currentRefreshToken);
        List<SessionMetaResponseDto> sessionMetas = new ArrayList<>();
        for(String key : keys){
            SessionMeta sessionMeta = redisSessionMeta.opsForValue().get(key);
            if(sessionMeta == null) continue;
            String keyHash = key.substring(key.lastIndexOf(":") + 1);
            boolean isCurrent = currentTokenHash.equals(keyHash);
            SessionMetaResponseDto sessionMetaResponseDto = new SessionMetaResponseDto(
                    sessionMeta.getSessionId(),
                    sessionMeta.getDeviceName(),
                    sessionMeta.getDeviceType(),
                    sessionMeta.getUserAgent(),
                    sessionMeta.getLoginAt(),
                    isCurrent
            );
            sessionMetas.add(sessionMetaResponseDto);
        }
        return sessionMetas;
    }
}
