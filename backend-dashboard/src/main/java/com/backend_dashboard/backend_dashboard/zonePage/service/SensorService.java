package com.backend_dashboard.backend_dashboard.zonePage.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SensorService {
    // 센서 데이터를 상태로 변환하는 함수
    public String getStatusBySensorData(String sensorType, Map<String, Double> val) {
        boolean isGreen = false;
        boolean isYellow = false;
        boolean isRed = false;

        switch (sensorType) {
            // 온도
            case ("temperature"):
                double temp = val.get("value");
                isGreen = ( 20.0 <= temp && temp <= 22.0 );
                isYellow = (( 19.0 <= temp && temp < 20.0 ) || ( 22.0 < temp && temp <= 24.0 ));
                isRed = !isGreen && !isYellow;
                break;

            // 습도
            case ("humidity"):
                double humi = val.get("value");
                isGreen = ( 40.0 <= humi && humi <= 50.0);
                isYellow = (( 32.0 <= humi && humi < 40.0 ) || ( 50.0 < humi && humi <= 52.0 ));
                isRed = !isGreen && !isYellow;
                break;

            // 풍향
            case ("windDir"):
                double wind = val.get("value");
                isGreen = ( -14 <= wind && wind <= 14 );
                isYellow = (( -20 <= wind && wind < -14 ) || ( 14 < wind && wind <= 20 ));
                isRed = !isGreen && !isYellow;
                break;

            // 정전기
            case ("esd"):
                double esd = val.get("value");
                isGreen = ( 30 <= esd && esd <80 );
                isYellow = ( 80 <= esd && esd < 100 );
                isRed = ( 100 <= esd );
                break;

            // 파티클
            case ("particle"):
                // 입자 크기별로 다른 임계치 적용, 모든 수치가 각 임계치 범위 내여야 함
                double p01 = val.get("0.1");
                double p03 = val.get("0.3");
                double p05 = val.get("0.5");
                isGreen = ( 850 <= p01 && p01 < 1000) && ( 82 <= p03 && p03 < 102) && ( 20 <= p05 && p05 < 35);
                isYellow = ( 1000 <= p01 && p01 < 1045) && ( 102 <= p03 && p03 < 108) && ( 35 <= p05 && p05 < 39);
                isRed = ( 1045 <= p01 ) && ( 108 <= p03 ) && ( 39 <= p05 );
                break;
        }

        if (isGreen) return "green";
        if (isYellow) return "yellow";
        if(isRed) return "red";
        return "unknown";
    }

}
