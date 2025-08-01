package com.backend_dashboard.backend_dashboard.mainPage.service;

import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.ParticleSensorDataDto;
import lombok.RequiredArgsConstructor;
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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MainPageService {

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String INDEX = "iot-sensor-data";

    // Controller가 호출하는 메서드
    public Flux<SensorDataDto> getLatestSensorData(LocalDateTime fromTime) {
        return getRecentSensorData(fromTime)
                .collectList()
                .flatMapMany(list -> {
                    Map<String, SensorDataDto> latestPerSensor = new HashMap<>();
                    for (SensorDataDto dto : list) {
                        String sensorId = dto.getSensor_id();
                        SensorDataDto existing = latestPerSensor.get(sensorId);
                        if (existing == null || isAfter(dto, existing)) {
                            latestPerSensor.put(sensorId, dto);
                        }
                    }
                    return Flux.fromIterable(latestPerSensor.values());
                });
    }

    // 서비스 내부에서 사용하는 메서드
    private Flux<SensorDataDto> getRecentSensorData(LocalDateTime fromTime) {
        SearchRequest request = new SearchRequest(INDEX);

        RangeQueryBuilder rangeQuery = QueryBuilders
                .rangeQuery("timestamp")
                .gt(fromTime.toInstant(ZoneOffset.UTC).toString()); // ISO8601 UTC 기준

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .query(rangeQuery)
                .fetchSource(null, new String[]{"unit"})
                .sort("timestamp", SortOrder.ASC)
                .size(100);

        request.source(builder);

        return Mono.fromCallable(() -> client.search(request, RequestOptions.DEFAULT))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(response -> {
                    List<SensorDataDto> result = new ArrayList<>();
                    for (SearchHit hit : response.getHits()) {
                        try {
                            SensorDataDto dto = objectMapper.readValue(hit.getSourceAsString(), SensorDataDto.class);
                            result.add(dto);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return Flux.fromIterable(result);
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
}
