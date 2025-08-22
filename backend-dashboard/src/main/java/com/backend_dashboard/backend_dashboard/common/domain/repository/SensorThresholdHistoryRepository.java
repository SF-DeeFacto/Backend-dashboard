package com.backend_dashboard.backend_dashboard.common.domain.repository;

import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThresholdHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorThresholdHistoryRepository extends JpaRepository<SensorThresholdHistory, Long> {
}
