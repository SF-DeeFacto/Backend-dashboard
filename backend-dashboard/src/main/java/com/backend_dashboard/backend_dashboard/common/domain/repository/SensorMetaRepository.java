package com.backend_dashboard.backend_dashboard.common.domain.repository;

import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorMeta;
import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorResponseDto;
import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorResponseProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SensorMetaRepository extends JpaRepository<SensorMeta, String> {

//    @Query ("")
//    List<SensorResponseDto> findByIdAndUserScopeWithThreshold(@Param("scope") String scope);

//    @Query(
//            value = "SELECT m.sensor_id as sensorId, m.zone_id as zoneId, m.sensor_type as sensorType, " +
//                    "t.updated_at as updatedAt, t.updated_user_id as updatedUserId, " +
//                    "t.warning_low as warningLow, t.warning_high as warningHigh, " +
//                    "t.alert_low as alertLow, t.alert_high as alertHigh " +
//                    "FROM sensor_meta m " +
//                    "JOIN sensor_threshold t " +
//                    "ON m.zone_id = t.zone_id AND m.sensor_type = t.sensor_type " +
//                    "WHERE :scope LIKE CONCAT('%', m.zone_id, '%')",
//            nativeQuery = true
//    )
//    List<SensorResponseDto> findAllWithThresholdByUserScope(@Param("scope") String scope);

    @Query(
            value = "SELECT m.sensor_id as sensorId, m.zone_id as zoneId, m.sensor_type as sensorType, " +
                    "t.updated_at as updatedAt, t.updated_user_id as updatedUserId, " +
                    "t.warning_low as warningLow, t.warning_high as warningHigh, " +
                    "t.alert_low as alertLow, t.alert_high as alertHigh " +
                    "FROM sensor_meta m " +
                    "JOIN sensor_threshold t " +
                    "ON LEFT(m.zone_id, 1) = t.zone_id AND m.sensor_type = t.sensor_type " +
                    "WHERE :scope LIKE CONCAT('%', t.zone_id, '%')",
            nativeQuery = true
    )
    List<SensorResponseProjection> findAllWithThresholdByUserScope(@Param("scope") String scope);


}

