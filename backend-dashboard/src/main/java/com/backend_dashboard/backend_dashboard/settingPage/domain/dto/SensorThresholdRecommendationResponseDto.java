package com.backend_dashboard.backend_dashboard.settingPage.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SensorThresholdRecommendationResponseDto {

    private Long id;
    private String zoneId;
    private String sensorType;
    private Double warningLow;
    private Double warningHigh;
    private Double alertLow;
    private Double alertHigh;
    private String reasonTitle;
    private String reasonContent;
    private LocalDateTime recommendedAt;
    private Boolean appliedStatus;
    private LocalDateTime appliedAt;

    // 추천받은 당시 사용 중인 Threshold 정보
    private Long currentThresholdId;
    private Double currentWarningLow;
    private Double currentWarningHigh;
    private Double currentAlertLow;
    private Double currentAlertHigh;
}
