package com.backend_dashboard.backend_dashboard.settingPage.domain.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SensorThresholdResponseDto {
    private String zoneId;
    private String sensorType;
    private String reasonTitle;
    private String reasonContent;
    private Double warningLow;
    private Double warningHigh;
    private Double alertLow;
    private Double alertHigh;
    private String updatedUserId;
    private LocalDateTime updatedAt;
}
