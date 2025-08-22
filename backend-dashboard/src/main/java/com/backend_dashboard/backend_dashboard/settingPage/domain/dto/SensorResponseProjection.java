package com.backend_dashboard.backend_dashboard.settingPage.domain.dto;

import java.time.LocalDateTime;

public interface SensorResponseProjection {
    String getSensorId();
    String getZoneId();
    String getSensorType();
    LocalDateTime getUpdatedAt();
    String getUpdatedUserId();
    Double getWarningLow();
    Double getWarningHigh();
    Double getAlertLow();
    Double getAlertHigh();
}
