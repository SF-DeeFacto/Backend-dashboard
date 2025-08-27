package com.backend_dashboard.backend_dashboard.settingPage.service;

import com.backend_dashboard.backend_dashboard.common.exception.CustomException;
import com.backend_dashboard.backend_dashboard.common.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class WeatherService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${spring.api.weather-api-key}")
    private String weatherApiKey;  // 필드 주입


    // 1시간 단위로 날씨 호출 스케쥴러 실행
    @Scheduled(cron = "0 0 * * * *")
    public void updateWeatherCache() {
        fetchAndCacheWeather(); // API 호출 → Redis 저장
    }

    // 날씨 API 호출
    public Map<String, Object> fetchAndCacheWeather() {
        // 1. 외부 API 호출
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=37.55893664&lon=126.9987376&appid=" + weatherApiKey;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null) {
            Map<String, Object> data = extractWeatherData(response);

            try {
                String jsonData = objectMapper.writeValueAsString(data);
                redisTemplate.opsForValue().set("weather:seoul", jsonData, 1, TimeUnit.HOURS);
            } catch (JsonProcessingException e) {
                throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);
            }
        }
        return response;
    }

    // 필요한 데이터만 추출
    private Map<String, Object> extractWeatherData(Map<String, Object> apiResponse) {
        Map<String, Object> simplified = new HashMap<>();

        // weather에 해당하는 값 추출
        List<Map<String, Object>> weatherList = (List<Map<String, Object>>) apiResponse.get("weather");
        if (weatherList != null && !weatherList.isEmpty()) {
            simplified.put("main", weatherList.get(0).get("main"));
            simplified.put("description", weatherList.get(0).get("description"));
            simplified.put("icon", weatherList.get(0).get("icon"));
        }

        // 온도 추출 후 섭씨로 변환
        Map<String, Object> main = (Map<String, Object>) apiResponse.get("main");
        if (main != null) {
            double tempKelvin = ((Number) main.get("temp")).doubleValue();
            double tempCelsius = tempKelvin - 273.15;

            simplified.put("temp", Math.round(tempCelsius * 10) / 10.0); // 소수점 1자리 반올림
        }

        simplified.put("timestamp", apiResponse.get("dt"));
        return simplified;
    }

    // Redis에서 weather 정보 조회
    public Map<String, Object> getWeatherData() {
        String cachedJson = (String) redisTemplate.opsForValue().get("weather:seoul");
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, Map.class); // JSON 문자열을 Map으로 변환
            } catch (JsonProcessingException e) {
                throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);
            }
        }

        // 캐시가 없으면 API 호출 및 Redis 저장
        fetchAndCacheWeather();

        // 다시 Redis에서 JSON 문자열을 읽어와서 Map으로 변환
        cachedJson = (String) redisTemplate.opsForValue().get("weather:seoul");
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, Map.class);
            } catch (JsonProcessingException e) {
                throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);
            }
        }

        return new HashMap<>();
    }
}
