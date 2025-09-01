package com.backend_dashboard.backend_dashboard.common.domain.repository;

import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThresholdRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SensorThresholdRecommendationRepository extends JpaRepository<SensorThresholdRecommendation, Long> {

    @Query(
            value = "SELECT * " +
                    "FROM sensor_threshold_recommendation t " +
                    "WHERE (:scope LIKE CONCAT('%', LEFT(t.zone_id, 1), '%')) " +
                    "AND (:sensorType IS NULL OR t.sensor_type = :sensorType) " +
                    "AND (:zoneId IS NULL OR LEFT(t.zone_id, 1) = :zoneId)",
            nativeQuery = true
    )
    List<SensorThresholdRecommendation> findAllByUserScope(
            @Param("scope") String scope,
            @Param("sensorType") String sensorType,
            @Param("zoneId") String zoneId
    );
}
