package com.backend_dashboard.backend_dashboard.zonePage.controller;

import com.backend_dashboard.backend_dashboard.zonePage.dto.GroupSensorWithStatusDto;
import com.backend_dashboard.backend_dashboard.zonePage.service.ZonePageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ZonePageController {
    private final ZonePageService zonePageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/home", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<GroupSensorWithStatusDto>> getZoneSensorData(
            @RequestParam String zoneId
    ) {
        Instant start = Instant.now().minus(Duration.ofMinutes(3));
        System.out.println(start);
        return Flux.interval(java.time.Duration.ofSeconds(2))
                .flatMap(tick -> {
                    Instant fromTime = start.plus(Duration.ofSeconds(tick*2));
                    return zonePageService.getSensorDataWithStatus(fromTime, zoneId)
                            .onErrorResume(e -> {
                                log.error("스트림 처리 중 오류 발생", e);
                                return Mono.just(List.of());
                            });
                });
    }
}