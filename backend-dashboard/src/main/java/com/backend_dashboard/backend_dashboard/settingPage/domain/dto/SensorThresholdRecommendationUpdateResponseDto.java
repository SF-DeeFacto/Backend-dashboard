package com.backend_dashboard.backend_dashboard.settingPage.domain.dto;

import com.backend_dashboard.backend_dashboard.common.domain.AppliedStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SensorThresholdRecommendationUpdateResponseDto {
    private Long id;
    private AppliedStatus appliedStatus;
    private LocalDateTime appliedAt;
}
