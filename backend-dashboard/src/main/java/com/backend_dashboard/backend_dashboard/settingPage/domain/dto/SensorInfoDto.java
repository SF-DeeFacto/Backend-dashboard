package com.backend_dashboard.backend_dashboard.settingPage.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SensorInfoDto {
    private String sensorId;
    private String zoneId;
    private String sensorType;
}
