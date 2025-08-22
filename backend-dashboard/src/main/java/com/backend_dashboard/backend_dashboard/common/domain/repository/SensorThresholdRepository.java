package com.backend_dashboard.backend_dashboard.common.domain.repository;

import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SensorThresholdRepository extends JpaRepository<SensorThreshold, Long> {
    Optional<SensorThreshold> findBySensorType(String sensorType);
    List<SensorThreshold> findAll();
}
