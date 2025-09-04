package com.backend_dashboard.backend_dashboard.zonePage.dto;

import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SensorWithStatusDto {
        private String sensorId;
        private String sensorType;
        private String sensorStatus;
        private String timestamp;
        private Map<String, Double> values;
    }

