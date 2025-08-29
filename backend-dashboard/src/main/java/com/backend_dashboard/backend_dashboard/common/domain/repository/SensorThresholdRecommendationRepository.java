package com.backend_dashboard.backend_dashboard.common.domain.repository;

import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThresholdRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorThresholdRecommendationRepository extends JpaRepository<SensorThresholdRecommendation, Long> {
}
