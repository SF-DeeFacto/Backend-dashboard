package com.backend_dashboard.backend_dashboard.settingPage.domain.dto;

import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThresholdRecommendation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorThresholdUpdateRequestDto {

    // zoneId와 sensorType은 어떤 센서인지 특정하기 위해 필수
    @NotBlank(message = "zoneId is compulsory")
    private String zoneId;
    @NotBlank(message = "sensorType is compulsory")
    private String sensorType;

    // 사용자가 수정 가능한 임계치 값
    @NotNull(message = "warningLow is compulsory")
    private Double warningLow;
    @NotNull(message = "warningHigh is compulsory")
    private Double warningHigh;
    @NotNull(message = "alertLow is compulsory")
    private Double alertLow;
    @NotNull(message = "alertHigh is compulsory")
    private Double alertHigh;
}
