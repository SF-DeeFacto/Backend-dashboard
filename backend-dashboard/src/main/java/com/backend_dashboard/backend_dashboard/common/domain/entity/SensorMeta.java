package com.backend_dashboard.backend_dashboard.common.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sensor_meta")
public class SensorMeta {
    @EmbeddedId
    private SensorMetaId id;

    @Column(name = "zone_id")
    private String zoneId;
}

