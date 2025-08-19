package com.backend_dashboard.backend_dashboard.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCacheDto {
    private Long Id;
    private String employeeId;
    private String role;
    private String shift;
    private String name;
}
