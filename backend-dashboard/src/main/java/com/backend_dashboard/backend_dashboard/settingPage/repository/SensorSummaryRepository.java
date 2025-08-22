package com.backend_dashboard.backend_dashboard.settingPage.repository;

import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorInfoDto;
import com.backend_dashboard.backend_dashboard.settingPage.domain.entity.SensorSummary;
import com.backend_dashboard.backend_dashboard.settingPage.domain.entity.SensorSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorSummaryRepository extends JpaRepository<SensorSummary, SensorSummaryId> {

    // 전체 distinct sensorId, zoneId, sensorType 조회
    @Query("SELECT DISTINCT new com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorInfoDto(" +
            "s.id.sensorId, s.id.zoneId, s.id.sensorType) " +
            "FROM SensorSummary s ORDER BY s.id.sensorId")
    List<SensorInfoDto> findDistinctSensorIdAndZones();

    // sensorType으로 distinct 조회
    @Query("SELECT DISTINCT new com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorInfoDto(" +
            "s.id.sensorId, s.id.zoneId, s.id.sensorType) " +
            "FROM SensorSummary s " +
            "WHERE s.id.sensorType = :sensorType " +
            "ORDER BY s.id.sensorId")
    List<SensorInfoDto> findDistinctByType(@Param("sensorType") String sensorType);

    // zoneId로 distinct 조회
    @Query("SELECT DISTINCT new com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorInfoDto(" +
            "s.id.sensorId, s.id.zoneId, s.id.sensorType) " +
            "FROM SensorSummary s " +
            "WHERE s.id.zoneId = :zoneId " +
            "ORDER BY s.id.sensorId")
    List<SensorInfoDto> findDistinctByZone(@Param("zoneId") String zoneId);

    // sensorType + zoneId로 distinct 조회
    @Query("SELECT DISTINCT new com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorInfoDto(" +
            "s.id.sensorId, s.id.zoneId, s.id.sensorType) " +
            "FROM SensorSummary s " +
            "WHERE s.id.sensorType = :sensorType AND s.id.zoneId = :zoneId " +
            "ORDER BY s.id.sensorId")
    List<SensorInfoDto> findDistinctByTypeAndZone(@Param("sensorType") String sensorType,
                                                  @Param("zoneId") String zoneId);
}
