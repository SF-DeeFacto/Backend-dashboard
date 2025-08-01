package com.backend_dashboard.backend_dashboard.zonePage.test;

import com.backend_dashboard.backend_dashboard.zonePage.dto.SensorDataDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.core.MainResponse;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.SortOrder;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.List;

public class OpenSearchTest {

    public static void main(String[] args) throws Exception {
        final String hostname = "localhost";
        final int port = 9200;
        final String scheme = "https";
        final String username = "admin";
        final String password = "StrongPassword22!";

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        // SSLContext 생성 (검증 무시)
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial((chain, authType) -> true)
                .build();

        RestClientBuilder builder = RestClient.builder(
                        new org.apache.http.HttpHost(hostname, port, scheme))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder
                                .setSSLContext(sslContext)
                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .setDefaultCredentialsProvider(credentialsProvider));

        RestHighLevelClient client = new RestHighLevelClient(builder);

        // 테스트 연결
        MainResponse response = client.info(RequestOptions.DEFAULT);
        System.out.println("Connected to OpenSearch! Cluster name: " + response.getClusterName());


        // 검색하여 데이터 받아오기
        String targetZoneId = "TEMP-001";

        SearchRequest searchRequest = new SearchRequest("sensor_data_stream");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termQuery("zone_id", "zone_B"));
        sourceBuilder.size(10);
        sourceBuilder.sort("timestamp", SortOrder.DESC);
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        // 센서 데이터를 담을 리스트
        List<SensorDataDto> sensorDataList = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();

        for (SearchHit hit : searchResponse.getHits()) {
            String json = hit.getSourceAsString();
            SensorDataDto dto = objectMapper.readValue(json, SensorDataDto.class); // Jackson 사용
            sensorDataList.add(dto);
        }

        // 출력
        sensorDataList.forEach(System.out::println);

//        if (searchResponse.getHits().getHits().length > 0) {
//            SearchHit hit = searchResponse.getHits().getHits()[0];
//            String sourceJson = hit.getSourceAsString();
//
//            ObjectMapper mapper = new ObjectMapper();
//            SensorDataDto data = mapper.readValue(sourceJson, SensorDataDto.class);
//
//            System.out.println("검색된 센서 데이터: " + data);
//        } else {
//            System.out.println("해당 zone_id의 데이터가 없습니다.");
//        }
        client.close();
    }
}
