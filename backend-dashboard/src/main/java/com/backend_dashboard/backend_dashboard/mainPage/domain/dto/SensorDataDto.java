package com.backend_dashboard.backend_dashboard.mainPage.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SensorDataDto {
    private String id;
    private String sensor_id;
    private String zone_id;
    private String timestamp;
    private String sensor_type;
//    private String unit;
    private float val;
    public String getSensorId() {
        return this.sensor_id;
    }
}