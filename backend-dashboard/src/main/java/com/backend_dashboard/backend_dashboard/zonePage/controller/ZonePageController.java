package com.backend_dashboard.backend_dashboard.zonePage.controller;

import com.backend_dashboard.backend_dashboard.common.domain.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.common.exception.CustomException;
import com.backend_dashboard.backend_dashboard.common.exception.ErrorCode;
import com.backend_dashboard.backend_dashboard.zonePage.dto.GroupSensorWithStatusDto;
import com.backend_dashboard.backend_dashboard.zonePage.service.ZonePageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.*;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
public class ZonePageController {
    private final ZonePageService zonePageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping(value = "/zone", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ApiResponseDto<List<GroupSensorWithStatusDto>>> getZoneSensorData(
            @RequestParam String zoneId,
            @RequestHeader(value = "X-Employee-Id") String employeeId
    ) {
        log.info("요청에 포함된 EmployeeId: {}", employeeId);

        final Instant[] lastFetchTime = {Instant.now(Clock.systemUTC()).minus(Duration.ofSeconds(5))};

        return zonePageService.checkScopeAndZoneId(employeeId, zoneId)
                .flatMapMany(isAuthenticated -> {
                    if (isAuthenticated) {
                        return Flux.interval(Duration.ofSeconds(5))
                                .flatMap(tick -> {
                                    Instant currentFetchTime = Instant.now(Clock.systemUTC());
                                    Instant fromTime = lastFetchTime[0];

                                    return zonePageService.getSensorDataWithStatus(fromTime, zoneId)
                                            .doOnNext(list -> {
                                                lastFetchTime[0] = currentFetchTime;
                                            })
                                            .onErrorResume(e -> {
                                                log.error("스트림 처리 중 오류 발생", e);
                                                return Mono.just(List.of());
                                            })
                                            .map(ApiResponseDto::createOk);
                                });
                    } else {
                        return Flux.error(new CustomException(ErrorCode.UNAUTHORIZED));
                    }
                });
    }
}