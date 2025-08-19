package com.backend_dashboard.backend_dashboard.settingPage.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_summary")
public class SensorSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime minute;

    @Column(name = "sensor_type")
    private String sensorType;

    private String unit;

    @Column(name = "sensor_id")
    private String sensorId;

    @Column(name = "zone_id")
    private String zoneId;

    @Column(name = "avg_val")
    private Double avgVal;

    @Column(name = "min_val")
    private Double minVal;

    @Column(name = "max_val")
    private Double maxVal;
}
