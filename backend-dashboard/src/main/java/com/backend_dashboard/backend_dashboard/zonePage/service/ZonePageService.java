package com.backend_dashboard.backend_dashboard.zonePage.service;

import com.backend_dashboard.backend_dashboard.common.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.GenericSensorDataDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.ParticleSensorDataDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.SensorDataDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.entity.SensorThreshold;
import com.backend_dashboard.backend_dashboard.mainPage.repository.SensorThresholdRepository;
import com.backend_dashboard.backend_dashboard.zonePage.dto.GroupSensorDataDto;
import com.backend_dashboard.backend_dashboard.zonePage.dto.GroupSensorWithStatusDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;

@Slf4j//@Slf4j // 로깅을 위해 추가
@Service
@RequiredArgsConstructor
public class ZonePageService {
    private final OpenSearchService openSearchService;
    private final SensorService sensorService;
    private final SensorThresholdRepository thresholdRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<List<GroupSensorWithStatusDto>> getSensorDataWithStatus(Instant fromTime, String zoneId) {
        // 1. 임계치 맵 가져오기 (캐싱 로직 포함)
        return getThresholdMap().flatMap(thresholdMap ->
                // 2. OpenSearchService에서 zoneId로 필터링된 센서 데이터 가져오기
                openSearchService.getLatestSensorData(fromTime, zoneId)
                        .collectList() // Flux -> Mono<List>로 변환
                        .map(sensorDataList -> {
                            // 3. (불필요한 zoneId 그룹화 과정 제거)
                            // 대신 타임스탬프를 초 단위로 그룹화
                            Map<String, List<SensorDataDto>> groupedByTimestamp = sensorDataList.stream()
                                    .collect(Collectors.groupingBy(
                                            data -> {
                                                Instant instant = Instant.parse(data.getTimestamp());
                                                // 초 단위로 자르고, 다시 String으로 포맷팅
                                                return instant.truncatedTo(ChronoUnit.SECONDS).toString();
                                            },
                                            Collectors.toList()
                                    ));

                            // 4. 그룹화된 데이터를 DTO로 변환
                            List<GroupSensorDataDto> groupedDtos = groupedByTimestamp.entrySet().stream()
                                    .map(entry -> {
                                        return new GroupSensorDataDto(entry.getKey(), entry.getValue());
                                    })
                                    .collect(Collectors.toList());

                            // 5. SensorService를 통해 상태를 추가하고 최종 DTO로 반환
                            return sensorService.addStatusToGroupedSensors(groupedDtos, thresholdMap);
                        })
        );
    }

    // 임계치 데이터 캐싱 적용
    @Cacheable("sensorThresholds")
    public Mono<Map<String, SensorThreshold>> getThresholdMap() {
        return Mono.fromCallable(() -> thresholdRepository.findAll().stream()
                        .collect(Collectors.toMap(SensorThreshold::getSensorType, t -> t)))
                .subscribeOn(Schedulers.boundedElastic()); // DB I/O는 별도 스레드에서 처리
    }

    // alert level 가져오기
    private boolean checkAlertThreshold(String type, Double val, Map<String, SensorThreshold> thresholdMap) {
        SensorThreshold threshold = thresholdMap.get(type);
        if (threshold == null) {
            System.out.println("임계치가 없습니다!!!!!!!!!!!!!!!!!!!!!!!!!");
            return false;
        }

        return (threshold.getAlertLow() != null && val < threshold.getAlertLow())
                || (threshold.getAlertHigh() != null && val > threshold.getAlertHigh());
    }

    // warning level 가져오기
    private boolean checkWarningThreshold(String type, Double val, Map<String, SensorThreshold> thresholdMap) {
        SensorThreshold threshold = thresholdMap.get(type);
        if (threshold == null) return false;

        return (threshold.getWarningLow() != null && val < threshold.getWarningLow())
                || (threshold.getWarningHigh() != null && val > threshold.getWarningHigh());
    }
}