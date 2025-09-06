package com.backend_dashboard.backend_dashboard.mainPage.controller;

import com.backend_dashboard.backend_dashboard.common.domain.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.ZoneStatusDto;
import com.backend_dashboard.backend_dashboard.mainPage.service.ZoneStatusService;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ZoneStatusService zoneStatusService;


    @GetMapping(value = "/home/status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamZoneStatuses() {
        final LocalDateTime[] lastFetchTime = {LocalDateTime.now(ZoneOffset.UTC).minusSeconds(3)};

        // 🖥️ 일정 간격(5초)으로 무한히 데이터 방출 (비동기) (무한 스트림으로 자동 정지 X)
        // flatMap: 각 원소 비동기 방식 처리 후, 단일 Flux로 병합해주는 연산자
        return Flux.interval(Duration.ofSeconds(5))
                .flatMap(tick -> {
                    LocalDateTime currentFetchTime = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1);
                    return zoneStatusService.getLatestSensorData(lastFetchTime[0])
                            .collectList()
                            // 🖥️ flatMapMany: Mono 내부 값 꺼내서 Flux로 변환하고 평탄화해주는 연산자
                            .flatMapMany(list -> {
                                if (!list.isEmpty()) {
                                    lastFetchTime[0] = currentFetchTime;
                                }
                                // 🖥️ 정해진 데이터를 순서대로 방출 (동기적)
                                return Flux.just(list);
                            });
                })
                .map(sensorList -> {
                    List<ZoneStatusDto> zoneStatuses = zoneStatusService.evaluateZoneStatuses(sensorList);
                    try {
                        ApiResponseDto<List<ZoneStatusDto>> response = ApiResponseDto.createOk(zoneStatuses, "MainPage: Red, Yellow, Green Logic Success");
                        return objectMapper.writeValueAsString(response);
                    } catch (JsonProcessingException e) {
                        return "";
                    }
                })
                .filter(msg -> !msg.isEmpty())
                .onErrorResume(e -> Flux.empty());
    }
}
