package com.backend_dashboard.backend_dashboard.remote.service;

import com.backend_dashboard.backend_dashboard.remote.dto.RecommendThresholdMessage;
import com.backend_dashboard.backend_dashboard.settingPage.service.SensorSettingService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendThresholdConsumer {

    private final SensorSettingService sensorSettingService;
    private final Validator validator;

    @KafkaListener(
            topics = "recommend-threshold",
            groupId = "dashboard-service-group",
            properties = {
                    JsonDeserializer.VALUE_DEFAULT_TYPE + ":com.backend_dashboard.backend_dashboard.remote.dto.RecommendThresholdMessage"
            }
    )
    public void comsumeRecommendThreshold(RecommendThresholdMessage response, Acknowledgment ack) {
        log.info("[RecommendThresholdConsumer] - Kafka Consume Success");
        Set<ConstraintViolation<RecommendThresholdMessage>> violations = validator.validate(response);
        if (!violations.isEmpty()) {
            log.warn("Validation failed: {}", violations);
            ack.acknowledge();
            return;
        }

        sensorSettingService.saveSensorThresholdRecommendation(response);
        log.info("[RecommendThresholdConsumer] - Kafka ConsumeRecommendThreshold save successful");
        ack.acknowledge();

    }
}