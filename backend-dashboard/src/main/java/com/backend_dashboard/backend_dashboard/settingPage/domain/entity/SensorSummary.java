package com.backend_dashboard.backend_dashboard.settingPage.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_summary")
public class SensorSummary {
    @EmbeddedId
    private SensorSummaryId id;   // 복합키

    private String unit;

    @Column(name = "avg_val")
    private Double avgVal;

    @Column(name = "min_val")
    private Double minVal;

    @Column(name = "max_val")
    private Double maxVal;
}
