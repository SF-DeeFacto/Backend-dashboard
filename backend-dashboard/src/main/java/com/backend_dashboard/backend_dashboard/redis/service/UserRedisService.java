package com.backend_dashboard.backend_dashboard.redis.service;


import com.backend_dashboard.backend_dashboard.common.exception.CustomException;
import com.backend_dashboard.backend_dashboard.common.exception.ErrorCode;
import com.backend_dashboard.backend_dashboard.redis.dto.UserCacheDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRedisService {
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "user:";

    public UserCacheDto getUserInfo(String employeeId) {
        String key = KEY_PREFIX + employeeId;
        // 캐스팅 없이 바로 UserCacheDto 타입으로 반환됩니다.
        Object obj = redisTemplate.opsForValue().get(key);
        UserCacheDto userInfo = objectMapper.convertValue(obj, UserCacheDto.class);
        if(userInfo == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        return userInfo;
    }

}
