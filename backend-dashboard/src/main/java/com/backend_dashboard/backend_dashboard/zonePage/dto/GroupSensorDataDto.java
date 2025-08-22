package com.backend_dashboard.backend_dashboard.zonePage.dto;

import com.backend_dashboard.backend_dashboard.common.domain.dto.SensorDataDto;
import lombok.*;

import java.util.List;

@Data
@Getter @Setter
@AllArgsConstructor
public class GroupSensorDataDto {
    private String timestamp;
    private List<SensorDataDto> seonsors;
}
