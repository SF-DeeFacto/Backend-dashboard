package com.backend_dashboard.backend_dashboard.settingPage.repository;

import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorInfoDto;
import com.backend_dashboard.backend_dashboard.settingPage.domain.entity.SensorSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorSummaryRepository extends JpaRepository<SensorSummary, Long> {
    @Query("SELECT DISTINCT new com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorInfoDto(s.sensorId, s.zoneId, s.sensorType)"+
    " FROM SensorSummary s ORDER BY s.sensorId")
    List<SensorInfoDto> findDistinctSensorIdAndZones();

    List<SensorInfoDto> findDistinctByType(String sensorType);
    List<SensorInfoDto> findDistinctByZone(String zoneId);
    List<SensorInfoDto> findDistinctByTypeAndZone(String sensorType, String zoneId);
}
