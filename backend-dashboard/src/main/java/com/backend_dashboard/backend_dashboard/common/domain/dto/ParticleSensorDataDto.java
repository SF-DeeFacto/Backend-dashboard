package com.backend_dashboard.backend_dashboard.common.domain.dto;

import lombok.Data;

@Data
public class ParticleSensorDataDto implements SensorDataDto {
    //   private String id;
    private String sensor_id;
    private String zone_id;
    private String timestamp;
    private String sensor_type;
    //    private String unit;
    private double val_0_1um;
    private double val_0_3um;
    private double val_0_5um;

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
