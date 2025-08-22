package com.backend_dashboard.backend_dashboard.settingPage.domain.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class SensorSummaryId implements Serializable {

    private LocalDateTime minute;

    @Column(name = "sensor_type", length = 100)
    private String sensorType;

    @Column(name = "sensor_id", length = 100)
    private String sensorId;

    @Column(name = "zone_id", length = 100)
    private String zoneId;

    // 기본 생성자 필수
    public SensorSummaryId() {}

    public SensorSummaryId(LocalDateTime minute, String sensorType, String sensorId, String zoneId) {
        this.minute = minute;
        this.sensorType = sensorType;
        this.sensorId = sensorId;
        this.zoneId = zoneId;
    }

    // equals, hashCode 반드시 구현
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SensorSummaryId)) return false;
        SensorSummaryId that = (SensorSummaryId) o;
        return Objects.equals(minute, that.minute) &&
                Objects.equals(sensorType, that.sensorType) &&
                Objects.equals(sensorId, that.sensorId) &&
                Objects.equals(zoneId, that.zoneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minute, sensorType, sensorId, zoneId);
    }
}

