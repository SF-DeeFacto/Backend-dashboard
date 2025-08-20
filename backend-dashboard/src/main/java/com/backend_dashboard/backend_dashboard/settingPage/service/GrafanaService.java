package com.backend_dashboard.backend_dashboard.settingPage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
public class GrafanaService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String GRAFANA_URL;
    private final String API_KEY;

    public GrafanaService(RestTemplate restTemplate,
                          ObjectMapper objectMapper,
                          @Value("${GRAFANA_URL}") String grafanaUrl,
                          @Value("${API_KEY}") String apiKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.GRAFANA_URL = grafanaUrl;
        this.API_KEY = apiKey;

    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public void updateThresholdsBySensorType(String sensorType, List<Map<String, Object>> newThresholds) {
        String searchUrl = GRAFANA_URL + "/api/search?type=dash-db";
        ResponseEntity<JsonNode> searchResponse = restTemplate.exchange(
                searchUrl,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders()),
                JsonNode.class);

        if(!searchResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("대시보드 목록을 가져오는 중 오류 발생: " + searchResponse.getStatusCode());
            return;
        }

        JsonNode dashboards = searchResponse.getBody();
        if(dashboards == null || !dashboards.isArray()) {
            System.out.println("대시보드 목록이 비어 있거나 형식이 올바르지 않습니다.");
        }
    }
}
