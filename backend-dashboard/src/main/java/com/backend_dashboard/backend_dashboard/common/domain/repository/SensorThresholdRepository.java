package com.backend_dashboard.backend_dashboard.common.domain.repository;

import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThreshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SensorThresholdRepository extends JpaRepository<SensorThreshold, Long> {
    List<SensorThreshold> findAll();
    Optional<SensorThreshold> findByZoneIdAndSensorType(String zondId, String sensorType);

    @Query("SELECT s FROM SensorThreshold s " +
            "WHERE :scope LIKE CONCAT('%', s.zoneId, '%')")
    List<SensorThreshold> findByUserScope(@Param("scope") String scope);
}
