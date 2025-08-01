package com.backend_dashboard.backend_dashboard.mainPage.domain.dto;

import lombok.Data;

@Data
public class GenericSensorDataDto implements SensorDataDto {
    //    private String id;
    private String sensor_id;
    private String zone_id;
    private String timestamp;
    private String sensor_type;
    //    private String unit;
    private double val;

    @Override
    public String getSensorId() {
        return sensor_id;
    }

    @Override
    public String getSensorType() {
        return sensor_type;
    }

    @Override
    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String getZoneId() {
        return zone_id;
    }
}
