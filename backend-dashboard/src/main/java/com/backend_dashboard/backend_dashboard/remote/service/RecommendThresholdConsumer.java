package com.backend_dashboard.backend_dashboard.remote.service;

import com.backend_dashboard.backend_dashboard.remote.dto.RecommendThresholdMessage;
import com.backend_dashboard.backend_dashboard.settingPage.service.SensorSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendThresholdConsumer {

    private final SensorSettingService sensorSettingService;

    @KafkaListener(
            topics = "recommend-threshold",
            groupId = "dashboard-service-group",
            properties = {
                    JsonDeserializer.VALUE_DEFAULT_TYPE + ":com.backend_dashboard.backend_dashboard.remote.dto.RecommendThresholdMessage"
            }
    )
    public void comsumeRecommendThreshold(RecommendThresholdMessage response, Acknowledgment ack) {
        log.info("kafka consume 성공");
        // 저장하는 로직 호출
        sensorSettingService.saveSensorThresholdRecommendation(response);
        ack.acknowledge();
    }
}