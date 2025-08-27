package com.backend_dashboard.backend_dashboard.zonePage.service;


import com.backend_dashboard.backend_dashboard.common.domain.dto.SensorDataDto;
import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThreshold;
import com.backend_dashboard.backend_dashboard.common.domain.repository.SensorThresholdRepository;
import com.backend_dashboard.backend_dashboard.common.exception.CustomException;
import com.backend_dashboard.backend_dashboard.common.exception.ErrorCode;
import com.backend_dashboard.backend_dashboard.zonePage.dto.GroupSensorDataDto;
import com.backend_dashboard.backend_dashboard.zonePage.dto.GroupSensorWithStatusDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j//@Slf4j // 로깅을 위해 추가
@Service
@RequiredArgsConstructor
public class ZonePageService {
    private final OpenSearchService openSearchService;
    private final SensorService sensorService;
    private final SensorThresholdRepository thresholdRepository;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<List<GroupSensorWithStatusDto>> getSensorDataWithStatus(Instant fromTime, String zoneId) {
        Mono<Map<String, SensorThreshold>> thresholdMapMono = getThresholdMap();

        return openSearchService.getLatestSensorData(fromTime, zoneId)
                .zipWith(thresholdMapMono)
                .map(tuple -> {
                    List<SensorDataDto> sensorDataList = tuple.getT1();
                    Map<String, SensorThreshold> thresholdMap = tuple.getT2();

                    if (sensorDataList.isEmpty()) {
                        return List.of();
                    }

                    Map<String, List<SensorDataDto>> groupedByTimestamp = sensorDataList.stream()
                            .collect(Collectors.groupingBy(
                                    data -> Instant.parse(data.getTimestamp()).truncatedTo(ChronoUnit.SECONDS).toString(),
                                    Collectors.toList()
                            ));

                    List<GroupSensorDataDto> groupedDtos = groupedByTimestamp.entrySet().stream()
                            .map(entry -> new GroupSensorDataDto(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());

                    return sensorService.addStatusToGroupedSensors(groupedDtos, thresholdMap);
                });
    }

    // 임계치 데이터 캐싱 적용
    @Cacheable("sensorThresholds")
    public Mono<Map<String, SensorThreshold>> getThresholdMap() {
        log.info("zone Thresholds 호출 시작");
        return Mono.fromCallable(() -> thresholdRepository.findAll().stream()
                        .collect(Collectors.toMap(
                                SensorThreshold::getSensorType,
                                t -> t,
                                (existing, replacement) -> existing // 👈 충돌 시 기존 값(existing) 유지
                        )))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // filter 대신 검증하는 로직
    public Mono<Boolean> checkScopeAndZoneId(String employeeId, String zoneId) {
        String key = "user:" + employeeId;
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .flatMap(value -> {
                    try {
                        JsonNode node = objectMapper.readTree(value);
                        String scopeStr = node.path("scope").asText();
                        List<String> scopes = Arrays.stream(scopeStr.split(","))
                                .map(String::trim)
                                .toList();
                        log.info("key:"+key);
                        log.info("Redis에서 읽어온 모든 scope: {}", scopes.stream().collect(Collectors.joining(", ")));
                        String zoneScope = String.valueOf(zoneId.toLowerCase().charAt(0));
                        log.info("zoneScope:"+zoneScope);
                        if (!scopes.contains(zoneScope)) {
                            return Mono.just(false);
                        }
                        return Mono.just(true);
                    } catch (JsonProcessingException e) {
                        return Mono.just(false);
                    }
                })
                .switchIfEmpty(Mono.error(new CustomException(ErrorCode.UNAUTHORIZED)));
    }
}