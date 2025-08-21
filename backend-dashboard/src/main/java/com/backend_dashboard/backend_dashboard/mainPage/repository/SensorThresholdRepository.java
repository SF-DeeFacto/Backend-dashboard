package com.backend_dashboard.backend_dashboard.mainPage.repository;

import com.backend_dashboard.backend_dashboard.mainPage.domain.entity.SensorThreshold;
import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorThresholdDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SensorThresholdRepository extends JpaRepository<SensorThreshold, String> {
    Optional<SensorThreshold> findBySensorType(String sensorType);
    List<SensorThreshold> findAll();
}
