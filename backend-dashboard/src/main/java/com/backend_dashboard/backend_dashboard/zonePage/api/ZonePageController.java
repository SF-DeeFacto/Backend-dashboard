package com.backend_dashboard.backend_dashboard.zonePage.api;

import com.backend_dashboard.backend_dashboard.common.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/home")
@RequiredArgsConstructor
public class ZonePageController {

//    @GetMapping(value = "/{zoneID}/sensors/status")
//    public ApiResponseDto<> getSensorDataAndStatus() {
//
//        return ;
//    }

}
