package com.backend_dashboard.backend_dashboard.remote.dto;

import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThresholdRecommendation;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecommendThresholdDto {
    // zoneId와 sensorType은 어떤 센서인지 특정하기 위해 필수
    private String zoneId;
    private String sensorType;

    private String reasonTitle;
    private String reasonContent;

    // 사용자가 수정 가능한 임계치 값
    private Double warningLow;
    private Double warningHigh;
    private Double alertLow;
    private Double alertHigh;

    public SensorThresholdRecommendation toThresholdRecommendationEntity() {
        return SensorThresholdRecommendation.builder()
                .zoneId(this.zoneId)
                .sensorType(this.sensorType)
                .reasonTitle(this.reasonTitle)
                .reasonContent(this.reasonContent)
                .warningHigh(this.warningHigh)
                .warningLow(this.warningLow)
                .alertHigh(this.alertHigh)
                .alertLow(this.alertLow)
                .build();
    }
}
