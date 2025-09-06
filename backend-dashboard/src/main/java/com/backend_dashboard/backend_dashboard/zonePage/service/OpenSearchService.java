package com.backend_dashboard.backend_dashboard.zonePage.service;


import com.backend_dashboard.backend_dashboard.common.domain.dto.GenericSensorDataDto;
import com.backend_dashboard.backend_dashboard.common.domain.dto.ParticleSensorDataDto;
import com.backend_dashboard.backend_dashboard.common.domain.dto.SensorDataDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchService {
    private final OpenSearchClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // zonePageService 에서 호출하는 함수
    public Mono<List<SensorDataDto>> getLatestSensorData(Instant fromTime, String zoneId) {
        return getRecentSensorDataFromMultipleIndices(fromTime, zoneId)
                .scan(new HashMap<String, SensorDataDto>(), (latestPerSensor, dto) -> {
                    String sensorId = dto.getSensorId();
                    SensorDataDto existing = latestPerSensor.get(sensorId);
                    if (existing == null || isAfter(dto, existing)) {
                        latestPerSensor.put(sensorId, dto);
                    }
                    return latestPerSensor;
                })
                .last()
                .map(map -> new ArrayList<>(map.values()));
    }

    // 최신 데이터로 확인
    private boolean isAfter(SensorDataDto a, SensorDataDto b) {
        try {
            Instant timeA = Instant.parse(a.getTimestamp());
            Instant timeB = Instant.parse(b.getTimestamp());
            return timeA.isAfter(timeB);
        } catch (Exception e) {
            log.error("시간 파싱 오류: {} 또는 {}", a.getTimestamp(), b.getTimestamp());
            return false;
        }
    }

    // 서비스 내부에서 사용하는 메서드, index 기반 오픈서치 연결
    public Flux<SensorDataDto> getRecentSensorDataFromMultipleIndices(Instant fromTime, String zoneId) {
        List<String> indices = List.of(
                "iot-winddirection",
                "iot-temperature",
                "iot-particle",
                "iot-humidity",
                "iot-electrostatic"
        );

        return Flux.merge(
                indices.stream()
                        .map(index -> searchFromIndexIfExists(index, fromTime, zoneId))
                        .collect(Collectors.toList())
        );
    }

    // 인덱스 5개 && 인덱스 존재하지 않을 경우에도 정상 동작
    private Flux<SensorDataDto> searchFromIndexIfExists(String index, Instant fromTime, String zoneId) {
        log.info("searchFromIndexIfExists 호출: index={}, fromTime={}, zoneId={}", index, fromTime, zoneId);

        return Mono.fromCallable(() -> {
                    // 인덱스 존재 확인
                    var existsResponse = client.indices().exists(e -> e.index(index));
                    if (!existsResponse.value()) {
                        return Collections.<SensorDataDto>emptyList();
                    }

                    // 검색 실행
                    SearchResponse<JsonNode> response = client.search(s -> s
                                    .index(index)
                                    .query(q -> q.bool(b -> b
                                            .must(m -> m.range(r -> r
                                                    .field("timestamp")
                                                    .gt(JsonData.of(fromTime.toString()))
                                            ))
                                            .must(m -> m.term(t -> t
                                                    .field("zone_id.keyword")
                                                    .value(FieldValue.of(zoneId))
                                            ))
                                    ))
                                    .sort(sort -> sort.field(f -> f
                                            .field("timestamp")
                                            .order(SortOrder.Asc)))
                                    .size(60)
                                    .source(src -> src.filter(f -> f.excludes("unit", "id"))),
                            JsonNode.class
                    );

                    log.info("인덱스 {} 검색 결과: {}개", index, response.hits().hits().size());

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
                            log.error("DTO 변환 중 오류 발생, 소스 데이터: {}", hit.source(), e);
                            throw new RuntimeException("DTO 변환 오류", e);
                        }
                    }
                    return result;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .onErrorResume(e -> {
                    log.error("OpenSearch 검색 실패", e);
                    return Flux.empty();
                });
    }

}
