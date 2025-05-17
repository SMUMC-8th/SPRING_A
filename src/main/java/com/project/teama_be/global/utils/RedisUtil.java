package com.project.teama_be.global.utils;

import com.project.teama_be.domain.notification.dto.NotiReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(String key, Object val, Long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, val, time, timeUnit);
    }

    public void saveFcmToken(String key, NotiReqDTO.FcmToken token){
        redisTemplate.opsForValue().set(key, token);
    }

    public NotiReqDTO.FcmToken getFcmToken(String key) {
        return (NotiReqDTO.FcmToken) redisTemplate.opsForValue().get(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) { redisTemplate.delete(key); }

}
