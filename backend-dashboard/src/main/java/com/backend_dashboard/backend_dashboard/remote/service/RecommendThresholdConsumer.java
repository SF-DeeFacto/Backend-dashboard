package com.backend_dashboard.backend_dashboard.remote.service;

import com.backend_dashboard.backend_dashboard.remote.dto.RecommendThresholdMessage;
import com.backend_dashboard.backend_dashboard.settingPage.service.SensorSettingService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
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
    public void comsumeRecommendThreshold(@Valid RecommendThresholdMessage response, Acknowledgment ack) {
        log.info("kafka consume 성공");
        try {
            // 유효성 검증된 DTO 처리
            sensorSettingService.saveSensorThresholdRecommendation(response);
            log.info("kafka recommend threshold save successful");
            ack.acknowledge();
        } catch (ConstraintViolationException | MethodArgumentNotValidException e) {
            log.warn("Validation failed: {}", e.getMessage());
            // 필요 시 Dead Letter Topic으로 전송하거나 무시
        }

    }
}