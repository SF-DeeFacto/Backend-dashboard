package com.backend_dashboard.backend_dashboard.settingPage.domain.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorThresholdUpdateRequestDto {

    // zoneId와 sensorType은 어떤 센서인지 특정하기 위해 필수
    private String zoneId;
    private String sensorType;

    // 사용자가 수정 가능한 임계치 값
    private Double warningLow;
    private Double warningHigh;
    private Double alertLow;
    private Double alertHigh;
}
