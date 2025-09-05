package com.backend_dashboard.backend_dashboard.settingPage.domain.dto;

import com.backend_dashboard.backend_dashboard.common.domain.AppliedStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SensorThresholdRecommendationUpdateRequestDto {
    private Long recommendId;
    private AppliedStatus appliedStatus;

}
