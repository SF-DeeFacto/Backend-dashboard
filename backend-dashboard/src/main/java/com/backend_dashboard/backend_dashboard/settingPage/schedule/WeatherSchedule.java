package com.backend_dashboard.backend_dashboard.settingPage.schedule;

import com.backend_dashboard.backend_dashboard.settingPage.service.WeatherService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@EnableScheduling
public class WeatherSchedule {
    private final WeatherService weatherService;

    // 1시간 단위로 날씨 호출 스케쥴러 실행
    @Scheduled(cron = "0 0 * * * *")
    public void updateWeatherCache() {
        weatherService.fetchAndCacheWeather(); // API 호출 → Redis 저장
    }
}
