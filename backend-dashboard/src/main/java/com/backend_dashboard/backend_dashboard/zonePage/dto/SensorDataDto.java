package com.backend_dashboard.backend_dashboard.zonePage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorDataDto {
    @JsonProperty("id")
    private String id;

    @JsonProperty("sensor_id")
    private String sensorId;

    @JsonProperty("zone_id")
    private String zoneId;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("sensor_type")
    private String type;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("val")
    private double value;

//    private String type;
//    private String sensorId;
//    private String status;
//    private double value;
//    private Map<String, Double> values;
}
