package com.backend_dashboard.backend_dashboard.mainPage.service;

import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.GenericSensorDataDto;
import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.ParticleSensorDataDto;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.SensorDataDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

// 임시 서비스 (opensearch 테스트용)
@Service
@RequiredArgsConstructor
public class MainPageService {

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
                "iot-sensor-data",  // 🔥 AWS opensearch 임시 Index
                "sensor_data_stream",  // local opensearch 임시 Index (temp, humi, esd, windDir)
                "particle_sensor_data_stream",  // local opensearch 임시 Index (particle)
                "temp_sensor_data_stream",
                "humi_sensor_data_stream",
                "esd_sensor_data_stream",
                "winddir_sensor_data_stream"
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
                            // local - AWS 에 따라서 조절 필요
                            .fetchSource(null, new String[]{"unit", "id"})
                            .sort("timestamp", SortOrder.ASC)
                            .size(100);

                    request.source(builder);

                    var response = client.search(request, RequestOptions.DEFAULT);

//                    List<SensorDataDto> result = new ArrayList<>();
//                    for (SearchHit hit : response.getHits()) {
//                        try {
//                            SensorDataDto dto = objectMapper.readValue(hit.getSourceAsString(), SensorDataDto.class);
//                            result.add(dto);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
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
                            e.printStackTrace(); // 혹은 로그 처리
                        }
                    }
                    return result;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                // 에러나면 빈 Flux로 대체해서 병합 시 전체 중단 방지
                .onErrorResume(e -> Flux.empty());
    }

}
