package com.backend_dashboard.backend_dashboard.mainPage.domain.dto;

import lombok.Data;

@Data
public class ParticleSensorDataDto {
//    private String id;
    private String sensor_id;
    private String zone_id;
    private String timestamp;
    private String sensor_type;
    //    private String unit;
    private double val_0_1;
    private double val_0_3;
    private double val_0_5;

    public String getSensorId() {
        return this.sensor_id;
    }
}
