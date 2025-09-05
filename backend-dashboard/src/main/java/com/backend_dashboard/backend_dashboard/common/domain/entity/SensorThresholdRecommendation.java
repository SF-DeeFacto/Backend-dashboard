package com.backend_dashboard.backend_dashboard.common.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor   // JPA 필수
@AllArgsConstructor  // Builder와 함께 사용 시 편리
@Table(
        name = "sensor_threshold_recommendation"
)
public class SensorThresholdRecommendation {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "zone_id", nullable = false)
        private String zoneId;

        @Column(name = "sensor_type", nullable = false)
        private String sensorType;

        @Column(name = "warning_low")
        private Double warningLow;

        @Column(name = "warning_high", nullable = false)
        private Double warningHigh;

        @Column(name = "alert_low")
        private Double alertLow;

        @Column(name = "alert_high", nullable = false)
        private Double alertHigh;

        @Column(name = "reason_title")
        private String reasonTitle;

        @Column(name = "reason_content")
        private String reasonContent;

        @Column(name="recommended_at")
        private LocalDateTime recommendedAt;

        @Column(name = "applied_status")
        private Boolean appliedStatus;

        @Column(name = "applied_at")
        private LocalDateTime appliedAt;

        @Column(name = "current_threshold_id")
        private Long currentThresholdId;
}
