package com.backend_dashboard.backend_dashboard.remote.dto;

import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorThresholdUpdateRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class RecommendThresholdMessage {
    @NotBlank(message = "zoneId is compulsory")
    private String zoneId;

    @Valid
    @NotEmpty(message = "sensorThresholdUpdateRequestDto List is compulsory")
    private List<SensorThresholdUpdateRequestDto> sensorThresholdUpdateRequestDto;  // 센서 타입별 임계치 리스트

    @NotNull(message = "recommendedAt is compulsory")
    private LocalDateTime recommendedAt;
}