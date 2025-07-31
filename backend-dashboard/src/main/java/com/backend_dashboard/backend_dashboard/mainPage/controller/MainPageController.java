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
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MainPageController {

    private final MainPageService sensorDataService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/sensor-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamSensorData2() {
        // 각 클라이언트 요청별로 lastFetchTime 초기화 (1초 전)
        final LocalDateTime[] lastFetchTime = {LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1)};

        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(tick -> {
                    LocalDateTime currentFetchTime = LocalDateTime.now(ZoneOffset.UTC);
                    return sensorDataService.getRecentSensorData(lastFetchTime[0])
                            .collectList()
                            .flatMapMany(list -> {
                                lastFetchTime[0] = currentFetchTime; // 업데이트
//                                return Flux.fromIterable(list);
                                return Flux.just(list);
                            });
                })
                .map(data -> {
                    try {
                        ApiResponseDto<List<SensorDataDto>> response = ApiResponseDto.createOk(data);
                        String json = objectMapper.writeValueAsString(response);
                        return json;
                    } catch (Exception e) {
                        return "";
                    }
                })
                .filter(msg -> !msg.trim().isEmpty())
                .onErrorResume(e -> Flux.empty());
    }
}
