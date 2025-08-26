package com.backend_dashboard.backend_dashboard.zonePage.service;


import com.backend_dashboard.backend_dashboard.common.domain.dto.GenericSensorDataDto;
import com.backend_dashboard.backend_dashboard.common.domain.dto.ParticleSensorDataDto;
import com.backend_dashboard.backend_dashboard.common.domain.dto.SensorDataDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OpenSearchService {
    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenSearchService(RestHighLevelClient client) {
        this.client = client;
    }

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
        log.info("searchFromIndexIfExists 메서드 호출: index={}, fromTime={}, zoneId={}",index, fromTime, zoneId);
        return Mono.fromCallable(() -> {
                    // 인덱스 존재 확인
                    GetIndexRequest getIndexRequest = new GetIndexRequest(index);
                    boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
                    if (!exists) {
                        return Collections.<SensorDataDto>emptyList();
                    }

                    // 인덱스가 존재하면 검색 실행
                    SearchRequest request = new SearchRequest(index);
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                            .must(QueryBuilders.rangeQuery("timestamp").gt(fromTime))
                            .must(QueryBuilders.termQuery("zone_id.keyword", zoneId));

                    SearchSourceBuilder builder = new SearchSourceBuilder()
                            .query(boolQuery)
                            .fetchSource(null, new String[]{"unit", "id"})
                            .sort("timestamp", SortOrder.ASC)
                            .size(60);

                    request.source(builder);

                    var response = client.search(request, RequestOptions.DEFAULT);
                    log.info("인덱스 {} 검색 결과: {}개", index, response.getHits().getHits().length);

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
                            log.error("DTO 변환 중 오류 발생, 소스 데이터: {}", hit.getSourceAsString(), e);
                            throw new RuntimeException("DTO 변환 오류", e);
                        }
                    }
                    return result;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .onErrorResume(e -> Flux.empty());
    }

}
