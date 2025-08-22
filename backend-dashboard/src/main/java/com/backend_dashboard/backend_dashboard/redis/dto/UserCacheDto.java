package com.backend_dashboard.backend_dashboard.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCacheDto {
    // 고유식별번호
    private Long id;
    // 사번
    private String employeeId;
    // 이름
    private String name;
    // 권한
    private String role;
    // 구역 범위
    private String scope;
    // 근무시간
    private String shift;
}

