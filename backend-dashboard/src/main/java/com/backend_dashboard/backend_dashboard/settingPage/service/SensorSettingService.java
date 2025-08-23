package com.backend_dashboard.backend_dashboard.settingPage.service;

import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThreshold;
import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThresholdHistory;
import com.backend_dashboard.backend_dashboard.common.domain.repository.SensorMetaRepository;
import com.backend_dashboard.backend_dashboard.common.domain.repository.SensorThresholdHistoryRepository;
import com.backend_dashboard.backend_dashboard.common.domain.repository.SensorThresholdRepository;
import com.backend_dashboard.backend_dashboard.common.exception.CustomException;
import com.backend_dashboard.backend_dashboard.common.exception.ErrorCode;
import com.backend_dashboard.backend_dashboard.redis.dto.UserCacheDto;
import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorSettingService {
    private final SensorMetaRepository sensorMetaRepository;
    private final SensorThresholdRepository sensorThresholdRepository;
    private final SensorThresholdHistoryRepository sensorThresholdHistoryRepository;

    // 🖥️ 센서 목록 조회
    public Page<SensorResponseDto> getSensorList(UserCacheDto userInfo, String sensorType, String zoneId, Pageable pageable) {

        // User 권한 확인 (user's Scope 내부 sensor's ZoneId 포함 여부 확인)
        if(!isZoneInUserScope(userInfo, zoneId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // 셉서 목록 DB 조회
        List<SensorResponseProjection> projections = sensorMetaRepository.findAllWithThresholdByUserScope(userInfo.getScope(), sensorType, zoneId);

        // DTO 변환 (projections(repository 쿼리 결과 타입) -> SensorResponseDto)
        List<SensorResponseDto> sensorList = projections.stream()
                .map(p -> new SensorResponseDto(
                        p.getSensorId(), p.getZoneId(), p.getSensorType(),
                        p.getUpdatedAt(), p.getUpdatedUserId(),
                        p.getWarningLow(), p.getWarningHigh(),
                        p.getAlertLow(), p.getAlertHigh()
                ))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sensorList.size());
        List<SensorResponseDto> content = sensorList.subList(start, end);

        return new PageImpl<>(content, pageable, sensorList.size());
    }

    // 🖥️ 센서 임계치 조회
    public List<SensorThresholdResponseDto> getSensorThresholdList(UserCacheDto userInfo) {
        List<SensorThreshold> thresholdList = sensorThresholdRepository.findByUserScope(userInfo.getScope());
        return thresholdList.stream()
                .map(this::fromEntityToDTO) // 여기서 fromEntityToDTO 사용
                .collect(Collectors.toList());
    }

    // 🖥️ 센서 임계치 수정
    public SensorThresholdResponseDto updateSensorThreshold(SensorThresholdUpdateRequestDto request, UserCacheDto userInfo) {

        // User 권한 확인 (user's Scope 내부 수정 sensor's ZoneId 포함 여부 확인)
        if(!isZoneInUserScope(userInfo, request.getZoneId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        // 타겟 임계치 추출
        SensorThreshold target = sensorThresholdRepository.findByZoneIdAndSensorType(request.getZoneId(), request.getSensorType())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT));

        // 타겟 임계치 수정
        target.setWarningLow(request.getWarningLow());
        target.setWarningHigh(request.getWarningHigh());
        target.setAlertLow(request.getAlertLow());
        target.setAlertHigh(request.getAlertHigh());
        target.setUpdatedUserId(userInfo.getEmployeeId());
        target.setUpdatedAt(LocalDateTime.now());

        // 수정된 임계치 저장
        SensorThreshold updatedThreshold = sensorThresholdRepository.save(target);

        // 임계치 수정 로그 저장
        SensorThresholdHistory history = fromThresholdToHistory(updatedThreshold);
        sensorThresholdHistoryRepository.save(history);

        // 수정된 임계치 DTO 변환
        return fromEntityToDTO(updatedThreshold);
    }

    // 권한 확인 메소드 (User's Scope 내부 Sensor's ZoneId 포함 여부 확인)
    public Boolean isZoneInUserScope(UserCacheDto userInfo, String zoneId) {
        if(zoneId == null) {
            return true;
        }
        Set<String> zoneSet = Arrays.stream(userInfo.getScope().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        return zoneSet.contains(zoneId);
    }

    // SensorThreshold(ENTITY) -> SensorThresholdResponseDto(DTO)
    public SensorThresholdResponseDto fromEntityToDTO(SensorThreshold threshold) {
        return SensorThresholdResponseDto.builder()
                .zoneId(threshold.getZoneId())
                .sensorType(threshold.getSensorType())
                .warningLow(threshold.getWarningLow())
                .warningHigh(threshold.getWarningHigh())
                .alertLow(threshold.getAlertLow())
                .alertHigh(threshold.getAlertHigh())
                .updatedUserId(threshold.getUpdatedUserId())
                .updatedAt(threshold.getUpdatedAt())
                .build();
    }

    // SensorThreshold(ENTITY) -> SensorThresholdHistory(ENTITY)
    public SensorThresholdHistory fromThresholdToHistory(SensorThreshold threshold) {
        return SensorThresholdHistory.builder()
                .zoneId(threshold.getZoneId())
                .sensorType(threshold.getSensorType())
                .warningLow(threshold.getWarningLow())
                .warningHigh(threshold.getWarningHigh())
                .alertLow(threshold.getAlertLow())
                .alertHigh(threshold.getAlertHigh())
                .updatedUserId(threshold.getUpdatedUserId())
                .updatedAt(threshold.getUpdatedAt())
                .build();
    }
}
