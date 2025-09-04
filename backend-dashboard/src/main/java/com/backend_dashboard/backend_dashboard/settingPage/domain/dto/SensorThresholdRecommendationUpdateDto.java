package com.backend_dashboard.backend_dashboard.settingPage.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SensorThresholdRecommendationUpdateDto {
    private Long id;
    private Boolean appliedStatus;
    private LocalDateTime appliedAt;
}
