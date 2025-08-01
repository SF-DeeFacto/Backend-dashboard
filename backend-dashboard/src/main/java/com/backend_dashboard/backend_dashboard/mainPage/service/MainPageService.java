package com.backend_dashboard.backend_dashboard.mainPage.service;

import com.backend_dashboard.backend_dashboard.mainPage.domain.dto.SensorDataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MainPageService {

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

//    private static final String INDEX = "sensor_data_stream";
    private static final String INDEX = "iot-sensor-data";


    public Flux<SensorDataDto> getRecentSensorData(LocalDateTime fromTime) {
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
}
