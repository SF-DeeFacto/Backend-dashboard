package com.backend_dashboard.backend_dashboard.mainPage.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_threshold")
@Getter
@Setter
@NoArgsConstructor
public class SensorThreshold {

    @Id
    @Column(name = "sensor_type", nullable = false)
    private String sensorType;

    @Column(name = "warning_low")
    private Double warningLow;

    @Column(name = "warning_high")
    private Double warningHigh;

    @Column(name = "alert_low")
    private Double alertLow;

    @Column(name = "alert_high")
    private Double alertHigh;

    @Column(name = "updated_user_id")
    private String updatedUserId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
