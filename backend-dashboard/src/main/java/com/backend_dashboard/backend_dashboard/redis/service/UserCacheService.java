package com.backend_dashboard.backend_dashboard.redis.service;

import com.backend_dashboard.backend_dashboard.redis.dto.UserCacheDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCacheService {
    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper objectMapper;

    public UserCacheDto getUserCache(String employeeId) {
        try {
            String value = redisTemplate.opsForValue().get("user:"+employeeId);
            if(value== null) return null;
            return objectMapper.readValue(value, UserCacheDto.class);
        } catch (Exception e) {
            return null;
        }
    }


}
