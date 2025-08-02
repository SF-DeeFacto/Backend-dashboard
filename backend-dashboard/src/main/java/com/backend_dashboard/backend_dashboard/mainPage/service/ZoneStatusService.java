package com.backend_dashboard.backend_dashboard.mainPage.service;

import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.GenericSensorDataDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.ParticleSensorDataDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.SensorDataDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.ZoneStatusDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.entity.SensorThreshold;
import com.backend_dashboard.backend_dashboard.mainPage.repository.SensorThresholdRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoneStatusService {

    private final MainPageService mainPageService;
    private final SensorThresholdRepository thresholdRepository;
    private final RestHighLevelClient client;
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
                "iot-sensor-data",   // 🔥 AWS opensearch 임시 Index
                "sensor_data_stream",  // local opensearch 임시 Index (temp, humi, esd, windDir)
                "particle_sensor_data_stream",  // local opensearch 임시 Index (particle)
                "sensor_data_stream_4",
                "sensor_data_stream_5"
        );

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
                    GetIndexRequest getIndexRequest = new GetIndexRequest(index);
                    boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
                    if (!exists) {
                        // 인덱스 없으면 빈 결과 리턴
                        return Collections.<SensorDataDto>emptyList();
                    }

                    // 인덱스가 존재하면 검색 실행
                    SearchRequest request = new SearchRequest(index);
                    RangeQueryBuilder rangeQuery = QueryBuilders
                            .rangeQuery("timestamp")
                            .gt(fromTime.toInstant(ZoneOffset.UTC).toString());

                    SearchSourceBuilder builder = new SearchSourceBuilder()
                            .query(rangeQuery)
                            .fetchSource(null, new String[]{"unit", "id"})
                            .sort("timestamp", SortOrder.ASC)
                            .size(100);

                    request.source(builder);

                    var response = client.search(request, RequestOptions.DEFAULT);
                    List<SensorDataDto> result = new ArrayList<>();
                    for (SearchHit hit : response.getHits()) {
                        try {
                            String json = hit.getSourceAsString();
                            JsonNode node = objectMapper.readTree(json);
                            String sensorType = node.get("sensor_type").asText();

                            SensorDataDto dto;
                            if ("particle".equals(sensorType)) {
                                dto = objectMapper.treeToValue(node, ParticleSensorDataDto.class);
                            } else {
                                dto = objectMapper.treeToValue(node, GenericSensorDataDto.class);
                            }
                            result.add(dto);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return result;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                // 에러나면 빈 Flux로 대체해서 병합 시 전체 중단 방지
                .onErrorResume(e -> Flux.empty());
    }

    public List<ZoneStatusDto> evaluateZoneStatuses(List<SensorDataDto> sensors) {
        // zoneId -> 해당 zone의 센서 리스트 분리
        Map<String, List<SensorDataDto>> zoneMap = sensors.stream()
                .collect(Collectors.groupingBy(SensorDataDto::getZoneId));

        Map<String, SensorThreshold> thresholdMap = thresholdRepository.findAll().stream()
                .collect(Collectors.toMap(SensorThreshold::getSensorType, t -> t));

        List<ZoneStatusDto> results = new ArrayList<>();

        for (Map.Entry<String, List<SensorDataDto>> entry : zoneMap.entrySet()) {
            String zoneName = entry.getKey();
            List<SensorDataDto> sensorList = entry.getValue();

            String status = evaluateZoneStatus(sensorList, thresholdMap);
            results.add(new ZoneStatusDto(zoneName, status));
        }

        return results;
    }

    private String evaluateZoneStatus(List<SensorDataDto> sensors, Map<String, SensorThreshold> thresholdMap) {
        boolean hasRed = false;
        boolean hasYellow = false;

        for (SensorDataDto dto : sensors) {
            String type = dto.getSensorType();

            if (type.startsWith("particle")) {
                ParticleSensorDataDto pDto = (ParticleSensorDataDto) dto;

                if (checkAlertThreshold("particle_0_1", pDto.getVal_0_1(), thresholdMap)) {
                    hasRed = true;
                } else if (checkAlertThreshold("particle_0_3", pDto.getVal_0_3(), thresholdMap)) {
                    hasRed = true;
                } else if (checkAlertThreshold("particle_0_5", pDto.getVal_0_5(), thresholdMap)) {
                    hasRed = true;
                } else if (checkWarningThreshold("particle_0_1", pDto.getVal_0_1(), thresholdMap)) {
                    hasYellow = true;
                } else if (checkWarningThreshold("particle_0_3", pDto.getVal_0_3(), thresholdMap)) {
                    hasYellow = true;
                } else if (checkWarningThreshold("particle_0_5", pDto.getVal_0_5(), thresholdMap)) {
                    hasYellow = true;
                }
            } else {
                double val = ((GenericSensorDataDto) dto).getVal();
                if (checkAlertThreshold(type, val, thresholdMap)) {
                    hasRed = true;
                } else if (checkWarningThreshold(type, val, thresholdMap)) {
                    hasYellow = true;
                }
            }
        }

        if (hasRed) return "RED";
        if (hasYellow) return "YELLOW";
        return "GREEN";
    }

    private boolean checkAlertThreshold(String type, Double val, Map<String, SensorThreshold> thresholdMap) {
        SensorThreshold threshold = thresholdMap.get(type);
        if (threshold == null) {
            System.out.println("임계치가 없습니다!!!!!!!!!!!!!!!!!!!!!!!!!");
            return false;
        }

        return (threshold.getAlertLow() != null && val < threshold.getAlertLow())
                || (threshold.getAlertHigh() != null && val > threshold.getAlertHigh());
    }

    private boolean checkWarningThreshold(String type, Double val, Map<String, SensorThreshold> thresholdMap) {
        SensorThreshold threshold = thresholdMap.get(type);
        if (threshold == null) return false;

        return (threshold.getWarningLow() != null && val < threshold.getWarningLow())
                || (threshold.getWarningHigh() != null && val > threshold.getWarningHigh());
    }
}
