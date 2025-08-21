package com.backend_dashboard.backend_dashboard.settingPage.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class SensorThresholdDto {
    private String sensorType;
    private Double warningLow;
    private Double warningHigh;
    private Double alertLow;
    private Double alertHigh;
    private String updatedUserUd;
    private LocalDateTime updatedAt;
}
