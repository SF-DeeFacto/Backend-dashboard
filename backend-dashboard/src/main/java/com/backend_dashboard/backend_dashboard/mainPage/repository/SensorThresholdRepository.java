package com.backend_dashboard.backend_dashboard.mainPage.repository;

import com.backend_dashboard.backend_dashboard.mainPage.domain.entity.SensorThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorThresholdRepository extends JpaRepository<SensorThreshold, String> {
    Optional<SensorThreshold> findBySensorType(String sensorType);
}
