package com.backend_dashboard.backend_dashboard.common.domain.entity;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "sensor_threshold_history")
public class SensorThresholdHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_id", nullable = false)
    private String zoneId;

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
    private LocalDateTime updatedAt = LocalDateTime.now();
}

