package com.backend_dashboard.backend_dashboard.mainPage.controller;

import com.backend_dashboard.backend_dashboard.common.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.SensorDataDto;
import com.backend_dashboard.backend_dashboard.mainPage.service.MainPageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class MainPageController {

    private final MainPageService sensorDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/sensor-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamSensorData() {
        final LocalDateTime[] lastFetchTime = {LocalDateTime.now(ZoneOffset.UTC).minusSeconds(2)};

        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(tick -> {
                    LocalDateTime currentFetchTime = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1);
                    return sensorDataService.getRecentSensorData(lastFetchTime[0])
                            .collectList()
                            .flatMapMany(list -> {
                                if (!list.isEmpty()) {
                                    // 데이터 있을 때만 업데이트
                                    lastFetchTime[0] = currentFetchTime;

                                    // sensor_id 기준 최신 데이터 필터링
                                    Map<String, SensorDataDto> latestPerSensor = new HashMap<>();
                                    for (SensorDataDto dto : list) {
                                        String sensorId = dto.getSensor_id();
                                        SensorDataDto existing = latestPerSensor.get(sensorId);
                                        // 최신 데이터만 저장 (timestamp 비교)
                                        if (existing == null || isAfter(dto, existing)) {
                                            latestPerSensor.put(sensorId, dto);
                                        }
                                    }
                                    // 최신 데이터만 리스트로 반환
                                    list = new ArrayList<>(latestPerSensor.values());
                                }
                                return Flux.just(list);
                            });
                })
                .map(data -> {
                    try {
                        ApiResponseDto<List<SensorDataDto>> response = ApiResponseDto.createOk(data);
                        return objectMapper.writeValueAsString(response);
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(msg -> !msg.trim().isEmpty())
                .onErrorResume(e -> Flux.empty());
    }

    // timestamp 비교 메서드
    private boolean isAfter(SensorDataDto a, SensorDataDto b) {
        try {
            LocalDateTime timeA = LocalDateTime.parse(a.getTimestamp());
            LocalDateTime timeB = LocalDateTime.parse(b.getTimestamp());
            return timeA.isAfter(timeB);
        } catch (Exception e) {
            return false;
        }
    }

}
