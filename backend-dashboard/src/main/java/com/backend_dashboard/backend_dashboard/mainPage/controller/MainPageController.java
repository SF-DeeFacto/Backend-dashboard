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

    private final MainPageService mainPageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/sensor-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamSensorData() {
        final LocalDateTime[] lastFetchTime = {LocalDateTime.now(ZoneOffset.UTC).minusSeconds(3)};

        return Flux.interval(Duration.ofSeconds(2))
                .flatMap(tick -> {
                    LocalDateTime currentFetchTime = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1);
                    return mainPageService.getLatestSensorData(lastFetchTime[0])
                            .collectList()
                            .flatMapMany(list -> {
                                if (!list.isEmpty()) {
                                    lastFetchTime[0] = currentFetchTime;
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
}
