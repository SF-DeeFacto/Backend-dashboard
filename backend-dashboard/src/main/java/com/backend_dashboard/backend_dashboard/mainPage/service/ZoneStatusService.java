package com.backend_dashboard.backend_dashboard.mainPage.service;

import com.backend_dashboard.backend_dashboard.common.domain.dto.GenericSensorDataDto;
import com.backend_dashboard.backend_dashboard.common.domain.dto.ParticleSensorDataDto;
import com.backend_dashboard.backend_dashboard.common.domain.dto.SensorDataDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.ZoneStatusDto;
import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThreshold;
import com.backend_dashboard.backend_dashboard.common.domain.repository.SensorThresholdRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoneStatusService {

    private final SensorThresholdRepository thresholdRepository;
    private final OpenSearchClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Controller가 호출하는 메서드
    public Flux<SensorDataDto> getLatestSensorData(LocalDateTime fromTime) {
        return getRecentSensorDataFromMultipleIndices(fromTime)
                .collectList()
                .flatMapMany(list -> {
                    Map<String, SensorDataDto> latestPerSensor = new HashMap<>();
                    for (SensorDataDto dto : list) {
                        String sensorId = dto.getSensorId();
                        SensorDataDto existing = latestPerSensor.get(sensorId);
                        if (existing == null || isAfter(dto, existing)) {
                            latestPerSensor.put(sensorId, dto);
                        }
                    }
                    // 🖥️ Iterable 객체로부터 Flux 생성 (동기적)
                    return Flux.fromIterable(latestPerSensor.values());
                });
    }

    private boolean isAfter(SensorDataDto a, SensorDataDto b) {
        try {
            LocalDateTime timeA = LocalDateTime.parse(a.getTimestamp());
            LocalDateTime timeB = LocalDateTime.parse(b.getTimestamp());
            return timeA.isAfter(timeB);
        } catch (Exception e) {
            return false;
        }
    }

    // 서비스 내부에서 사용하는 메서드
    public Flux<SensorDataDto> getRecentSensorDataFromMultipleIndices(LocalDateTime fromTime) {
        List<String> indices = List.of(
//                "iot-sensor-data",  // 🔥 AWS opensearch 임시 Index
                "iot-winddirection",
                "iot-temperature",
                "iot-particle",
                "iot-humidity",
                "iot-electrostatic"
//                "sensor_data_stream",  // local opensearch 임시 Index (temp, humi, esd, windDir)
//                "particle_sensor_data_stream",  // local opensearch 임시 Index (particle)
//                "temp_sensor_data_stream",
//                "humi_sensor_data_stream",
//                "esd_sensor_data_stream",
//                "winddir_sensor_data_stream"
        );

        // 🖥️ 병렬 처리 (여러 Flux를 동시에 병합하여 하나의 Flux화)
        // (순서 보장 X 각 Flux에서 오는 값 동시 처리)
        return Flux.merge(
                indices.stream()
                        .map(index -> searchFromIndexIfExists(index, fromTime))
                        .collect(Collectors.toList())
        );
    }

    // 인덱스 5개 && 인덱스 존재하지 않을 경우에도 정상 동작
    private Flux<SensorDataDto> searchFromIndexIfExists(String index, LocalDateTime fromTime) {
        return Mono.fromCallable(() -> {
                    // 인덱스 존재 확인
                    var existsResponse = client.indices().exists(e -> e.index(index));
                    if (!existsResponse.value()) {
                        log.info("인덱스 없음: {}", index);
                        return Collections.<SensorDataDto>emptyList();
                    }

                    // 검색 실행
                    String fromIso = fromTime.toInstant(ZoneOffset.UTC).toString();

                    SearchResponse<JsonNode> response = client.search(s -> s
                                    .index(index)
                                    .query(q -> q
                                            .range(r -> r
                                                    .field("timestamp")
                                                    .gt(JsonData.of(fromIso))
                                            )
                                    )
                                    .sort(sort -> sort
                                            .field(f -> f
                                                    .field("timestamp")
                                                    .order(SortOrder.Asc)
                                            )
                                    )
                                    .size(100)
                                    // unit, id 필드 제외 (include = null, exclude 지정)
                                    .source(src -> src.filter(f -> f.excludes("unit", "id"))),
                            JsonNode.class
                    );

                    List<SensorDataDto> result = new ArrayList<>();
                    for (Hit<JsonNode> hit : response.hits().hits()) {
                        try {
                            JsonNode node = hit.source();
                            if (node == null) continue;

                            String sensorType = node.get("sensor_type").asText();

                            SensorDataDto dto;
                            if ("particle".equals(sensorType)) {
                                dto = objectMapper.treeToValue(node, ParticleSensorDataDto.class);
                            } else {
                                dto = objectMapper.treeToValue(node, GenericSensorDataDto.class);
                            }
                            result.add(dto);
                        } catch (Exception e) {
                            log.error("JSON 파싱 오류", e);
                        }
                    }
                    return result;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .onErrorResume(ex -> {
                    log.error("OpenSearch 검색 오류", ex);
                    return Flux.empty();
                });
    }

    // Controller가 호출하는 메소드
    public List<ZoneStatusDto> evaluateZoneStatuses(List<SensorDataDto> sensors) {
        // zoneId -> 해당 zone의 센서 리스트 분리
        Map<String, List<SensorDataDto>> zoneMap = sensors.stream()
                .collect(Collectors.groupingBy(SensorDataDto::getZoneId));

        log.info("threshold findAll 시도 시작");

        Map<String, SensorThreshold> thresholdMap = thresholdRepository.findAll().stream()
                .collect(Collectors.toMap(
                        t -> t.getZoneId().toUpperCase() + "-" + t.getSensorType(),
                        t -> t
                ));

        log.info("findAll 결과, thresholdMap size: {}",thresholdMap.size());

        List<ZoneStatusDto> results = new ArrayList<>();

        // String: ZoneId (ZoneName)
        for (Map.Entry<String, List<SensorDataDto>> entry : zoneMap.entrySet()) {
            String zoneName = entry.getKey();
            List<SensorDataDto> sensorList = entry.getValue();

            // sensorList에는 {sensorId, sensorType, TimeStamp, zoneId}
            // thresholdMap에는 {key: ZoneId-SensorType, value: {id, zoneId, sensorType, warningLow, warningHigh, alertLow, alertHigh, updatedUserId, updatedAt}}
            String status = evaluateZoneStatus(sensorList, thresholdMap);
            results.add(new ZoneStatusDto(zoneName, status));
        }

        return results;
    }

    // sensorList에는 {sensorId, sensorType, TimeStamp, zoneId}
    // thresholdMap에는 {key: ZoneId-SensorType, value: {id, zoneId, sensorType, warningLow, warningHigh, alertLow, alertHigh, updatedUserId, updatedAt}}
    private String evaluateZoneStatus(List<SensorDataDto> sensors, Map<String, SensorThreshold> thresholdMap) {
        boolean hasRed = false;
        boolean hasYellow = false;

        for (SensorDataDto dto : sensors) {
            String zoneId = dto.getZoneId();
            String type = dto.getSensorType();

            if (type.startsWith("particle")) {
                ParticleSensorDataDto pDto = (ParticleSensorDataDto) dto;

                if (checkAlertThreshold("particle_0_1um", zoneId,  pDto.getVal_0_1um(), thresholdMap)) {
                    hasRed = true;
                } else if (checkAlertThreshold("particle_0_3um", zoneId, pDto.getVal_0_3um(), thresholdMap)) {
                    hasRed = true;
                } else if (checkAlertThreshold("particle_0_5um", zoneId, pDto.getVal_0_5um(), thresholdMap)) {
                    hasRed = true;
                } else if (checkWarningThreshold("particle_0_1um", zoneId, pDto.getVal_0_1um(), thresholdMap)) {
                    hasYellow = true;
                } else if (checkWarningThreshold("particle_0_3um", zoneId, pDto.getVal_0_3um(), thresholdMap)) {
                    hasYellow = true;
                } else if (checkWarningThreshold("particle_0_5um", zoneId, pDto.getVal_0_5um(), thresholdMap)) {
                    hasYellow = true;
                }
            } else {
                double val = ((GenericSensorDataDto) dto).getVal();
                if (checkAlertThreshold(type, zoneId, val, thresholdMap)) {
                    hasRed = true;
                } else if (checkWarningThreshold(type, zoneId, val, thresholdMap)) {
                    hasYellow = true;
                }
            }
        }

        if (hasRed) return "RED";
        if (hasYellow) return "YELLOW";
        return "GREEN";
    }

    private boolean checkAlertThreshold(String type, String zoneId, Double val, Map<String, SensorThreshold> thresholdMap) {
        String key = zoneId.charAt(0) + "-" + type;
        SensorThreshold threshold = thresholdMap.get(key);
        if (threshold == null) {
            System.out.println("임계치가 없습니다!!!!!!!!!!!!!!!!!!!!!!!!!");
            return false;
        }

        return (threshold.getAlertLow() != null && val < threshold.getAlertLow())
                || (threshold.getAlertHigh() != null && val > threshold.getAlertHigh());
    }

    private boolean checkWarningThreshold(String type, String zoneId, Double val, Map<String, SensorThreshold> thresholdMap) {
        String key = zoneId.charAt(0) + "-" + type;
        SensorThreshold threshold = thresholdMap.get(key);
        if (threshold == null) return false;

        return (threshold.getWarningLow() != null && val < threshold.getWarningLow())
                || (threshold.getWarningHigh() != null && val > threshold.getWarningHigh());
    }
}
