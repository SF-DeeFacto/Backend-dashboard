package com.backend_dashboard.backend_dashboard.zonePage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Data
@Getter @Setter
@AllArgsConstructor
public class GroupSensorWithStatusDto {
    private String timestamp;
    private List<SensorWithStatusDto> sensors;
}
