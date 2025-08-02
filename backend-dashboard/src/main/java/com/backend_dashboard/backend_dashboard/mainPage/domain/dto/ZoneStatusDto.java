package com.backend_dashboard.backend_dashboard.mainPage.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ZoneStatusDto {
    private String zoneName;
    private String status;
}
