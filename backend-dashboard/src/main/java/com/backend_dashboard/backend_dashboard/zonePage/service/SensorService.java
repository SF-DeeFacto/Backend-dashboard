package com.backend_dashboard.backend_dashboard.zonePage.service;


import com.backend_dashboard.backend_dashboard.common.domain.dto.GenericSensorDataDto;
import com.backend_dashboard.backend_dashboard.common.domain.dto.ParticleSensorDataDto;
import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThreshold;
import com.backend_dashboard.backend_dashboard.common.domain.repository.SensorThresholdRepository;
import com.backend_dashboard.backend_dashboard.zonePage.dto.GroupSensorDataDto;
import com.backend_dashboard.backend_dashboard.zonePage.dto.GroupSensorWithStatusDto;
import com.backend_dashboard.backend_dashboard.zonePage.dto.SensorWithStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorService {
    private final SensorThresholdRepository thresholdRepository;

    // 가져온 임계치로 상태 반환하기
    public List<GroupSensorWithStatusDto> addStatusToGroupedSensors(List<GroupSensorDataDto> groupedSensors, Map<String, SensorThreshold> thresholdMap) {

        return groupedSensors.stream()
                .map(group -> {
                    List<SensorWithStatusDto> sensorsWithStatus = group.getSeonsors().stream()
                            .map(sensor -> {

                                Map<String, Double> sensorValuesMap = new HashMap<>();

                                if (sensor instanceof ParticleSensorDataDto) {
                                    ParticleSensorDataDto particleSensor = (ParticleSensorDataDto) sensor;
                                    sensorValuesMap.put("0.1", particleSensor.getVal_0_1um());
                                    sensorValuesMap.put("0.3", particleSensor.getVal_0_3um());
                                    sensorValuesMap.put("0.5", particleSensor.getVal_0_5um());
                                } else if (sensor instanceof GenericSensorDataDto) {
                                    GenericSensorDataDto genericSensor = (GenericSensorDataDto) sensor;
                                    sensorValuesMap.put("value", genericSensor.getVal());
                                } else {
                                    // 알 수 없는 타입의 경우 빈 맵 반환
                                    sensorValuesMap = Collections.emptyMap();
                                }

                                String status = evaluateStatus(sensor.getSensorType(), sensorValuesMap, thresholdMap);

                                return new SensorWithStatusDto(
                                        sensor.getSensorId(),
                                        sensor.getSensorType(),
                                        status,
                                        sensor.getTimestamp(),
                                        sensorValuesMap
                                );
                            }).collect(Collectors.toList());

                    return new GroupSensorWithStatusDto(group.getTimestamp(), sensorsWithStatus);
                })
                .collect(Collectors.toList());
    }

    private String evaluateStatus(String type, Map<String, Double> values, Map<String, SensorThreshold> thresholdMap) {
        boolean hasRed = false;
        boolean hasYellow = false;

        if ("particle".equals(type)) {
            // particle의 val은 {"0.1": 950, "0.3": 100, "0.5": 30} 같은 구조
            if (checkAlertThreshold("particle_0_1um", values.get("0.1"), thresholdMap)) {
                hasRed = true;
            } else if (checkAlertThreshold("particle_0_3um", values.get("0.3"), thresholdMap)) {
                hasRed = true;
            } else if (checkAlertThreshold("particle_0_5um", values.get("0.5"), thresholdMap)) {
                hasRed = true;
            } else if (checkWarningThreshold("particle_0_1um", values.get("0.1"), thresholdMap)) {
                hasYellow = true;
            } else if (checkWarningThreshold("particle_0_3um", values.get("0.3"), thresholdMap)) {
                hasYellow = true;
            } else if (checkWarningThreshold("particle_0_5um", values.get("0.5"), thresholdMap)) {
                hasYellow = true;
            }

        } else {
            // generic 센서는 {"value": 21.0} 같은 구조
            Double val = values.get("value");

            if (checkAlertThreshold(type, val, thresholdMap)) {
                hasRed = true;
            } else if (checkWarningThreshold(type, val, thresholdMap)) {
                hasYellow = true;
            }
        }

        if (hasRed) return "RED";
        if (hasYellow) return "YELLOW";
        return "GREEN";
    }

    // alert level 가져오기
    private boolean checkAlertThreshold(String type, Double val, Map<String, SensorThreshold> thresholdMap) {
        log.info(type, val, thresholdMap);
        SensorThreshold threshold = thresholdMap.get(type);
        if (threshold == null) {
            System.out.println(type+"임계치가 없습니다!!!!!!!!!!!!!!!!!!!!!!!!!");
            return false;
        }

        return (threshold.getAlertLow() != null && val < threshold.getAlertLow())
                || (threshold.getAlertHigh() != null && val > threshold.getAlertHigh());
    }

    // warning level 가져오기
    private boolean checkWarningThreshold(String type, Double val, Map<String, SensorThreshold> thresholdMap) {
        SensorThreshold threshold = thresholdMap.get(type);
        if (threshold == null) return false;

        return (threshold.getWarningLow() != null && val < threshold.getWarningLow())
                || (threshold.getWarningHigh() != null && val > threshold.getWarningHigh());
    }


}
