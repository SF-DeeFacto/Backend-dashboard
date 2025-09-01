package com.backend_dashboard.backend_dashboard.settingPage.service;

import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThreshold;
import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThresholdHistory;
import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThresholdRecommendation;
import com.backend_dashboard.backend_dashboard.common.domain.repository.SensorMetaRepository;
import com.backend_dashboard.backend_dashboard.common.domain.repository.SensorThresholdHistoryRepository;
import com.backend_dashboard.backend_dashboard.common.domain.repository.SensorThresholdRecommendationRepository;
import com.backend_dashboard.backend_dashboard.common.domain.repository.SensorThresholdRepository;
import com.backend_dashboard.backend_dashboard.common.exception.CustomException;
import com.backend_dashboard.backend_dashboard.common.exception.ErrorCode;
import com.backend_dashboard.backend_dashboard.redis.dto.UserCacheDto;
import com.backend_dashboard.backend_dashboard.remote.dto.RecommendThresholdMessage;
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
    private final SensorThresholdRecommendationRepository sensorThresholdRecommendationRepository;

    // 🖥️ 센서 목록 조회
    public Page<SensorResponseDto> getSensorList(UserCacheDto userInfo, String sensorType, String zoneId, Pageable pageable) {

        // 관리자 권한 확인 (ROOT || ADMIN)
        if(!isAdmin(userInfo)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "You are not authorized to read sensor information");
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

        // 관리자 권한 확인 (ROOT || ADMIN)
        if(!isAdmin(userInfo)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "You are not authorized to read sensor threshold information");
        }

        // 센서 임계치 목록 DB 조회
        List<SensorThreshold> thresholdList = sensorThresholdRepository.findByUserScope(userInfo.getScope());

        // DTO 변환
        return thresholdList.stream()
                .map(this::fromEntityToDTO) // 여기서 fromEntityToDTO 사용
                .collect(Collectors.toList());
    }

    // 🖥️ 센서 임계치 수정
    public SensorThresholdResponseDto updateSensorThreshold(SensorThresholdUpdateRequestDto request, UserCacheDto userInfo) {

        // 관리자 권한 확인 (ROOT || ADMIN) && 수정 권한 확인
        if(!isAdmin(userInfo) || !hasAccess(userInfo, request.getZoneId())) {
            throw new CustomException(ErrorCode.FORBIDDEN, "You are not authorized to change sensor threshold information");
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

    // 🖥️ AI 추천된 센서 임계치 목록 저장 (Create)
    public void saveSensorThresholdRecommendation(RecommendThresholdMessage recommendThresholdMessage) {
        // Kafka Response 파싱
        String zoneId = recommendThresholdMessage.getZoneId();
        List<SensorThresholdUpdateRequestDto> recommendList = recommendThresholdMessage.getSensorThresholdUpdateRequestDto();
        LocalDateTime recommendAt = recommendThresholdMessage.getRecommendedAt();

        // recommendList(SensorThresholdUpdateRequestDto) -> SensorThresholdRecommendation -> Save
        for(SensorThresholdUpdateRequestDto dto: recommendList) {
            SensorThresholdRecommendation entity = dto.toThresholdRecommendationEntity();
            entity.setRecommendedAt(recommendAt);
            entity.setAppliedStatus(false);
            // 추천받은 일시 기준 적용되고 있는 임계치
            SensorThresholdHistory sensorThresholdHistory = sensorThresholdHistoryRepository.findTopByZoneIdAndSensorTypeOrderByUpdatedAtDesc(entity.getZoneId(), entity.getSensorType());
            if(sensorThresholdHistory!=null) {
                Long currentThresholdId = sensorThresholdHistory.getId();
                entity.setCurrentThresholdId(currentThresholdId);
            }
            log.info("Threshold Recommend entity made successfully: {}-{}", entity.getZoneId(), entity.getSensorType());
            sensorThresholdRecommendationRepository.save(entity);
            log.info("Threshold Recommend entity saved successfully: {}-{}", entity.getZoneId(), entity.getSensorType());
        }
    }

    // 🖥️ AI 추천된 센서 임계치 목록 조회 (Read)
    public PageImpl<SensorThresholdRecommendationDto> readSensorThresholdRecommendation(UserCacheDto userInfo, String sensorType, String zoneId, Pageable pageable) {

        // 관리자 권한 확인 (ROOT || ADMIN)
        if(!isAdmin(userInfo)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "You are not authorized to read sensor threshold information");
        }

        // 셉서 목록 DB 조회
        List<SensorThresholdRecommendation> recommends = sensorThresholdRecommendationRepository.findAllByUserScope(userInfo.getScope(), sensorType, zoneId);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), recommends.size());
        List<SensorThresholdRecommendation> pageSizeRecommends = recommends.subList(start, end);
        List<SensorThresholdRecommendationDto> response = new ArrayList<>();

        for(SensorThresholdRecommendation recommend: pageSizeRecommends) {
            SensorThresholdRecommendationDto dto
                    = SensorThresholdRecommendationDto
                    .builder()
                    .id(recommend.getId())
                    .zoneId(recommend.getZoneId())
                    .sensorType(recommend.getSensorType())
                    .warningLow(recommend.getWarningLow())
                    .warningHigh(recommend.getWarningHigh())
                    .alertLow(recommend.getAlertLow())
                    .alertHigh(recommend.getAlertHigh())
                    .recommendedAt(recommend.getRecommendedAt())
                    .appliedStatus(recommend.getAppliedStatus())
                    .appliedAt(recommend.getAppliedAt())
                    .build();

            if (recommend.getCurrentThresholdId() != null) {
                dto.setCurrentThresholdId(recommend.getCurrentThresholdId());
                Long currentThresholdId = recommend.getCurrentThresholdId();
                Optional<SensorThresholdHistory> currentThreshold = sensorThresholdHistoryRepository.findById(currentThresholdId);
                currentThreshold.ifPresent(threshold -> {
                    // threshold가 존재할 때만 실행됨
                    dto.setCurrentWarningLow(threshold.getWarningLow());
                    dto.setCurrentWarningHigh(threshold.getWarningHigh());
                    dto.setCurrentAlertLow(threshold.getAlertLow());
                    dto.setCurrentAlertHigh(threshold.getAlertHigh());
                });
            }
            response.add(dto);
        }

        return new PageImpl<>(response, pageable, response.size());
    }

    // 🖥️ AI 추천된 센서 임계치 목록 적용 (Update)

    //==================== <부가 기능 메소드> ====================

    // 관리자 권한 확인 메소드 (ROOT || ADMIN)
    public Boolean isAdmin(UserCacheDto userInfo) {
        return userInfo.getRole().equals("ROOT") || userInfo.getRole().equals("ADMIN");
    }

    // 관리자 권한 및 구역 권한 확인 메소드 (Root || Admin has Scope Access)
    public Boolean hasAccess(UserCacheDto userInfo, String zoneId) {
        boolean hasScopeAccess = Arrays.asList(userInfo.getScope().split(",")).contains(zoneId);
        return userInfo.getRole().equals("ROOT") || userInfo.getRole().equals("ADMIN") && hasScopeAccess;
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
