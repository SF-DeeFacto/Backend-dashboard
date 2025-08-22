package com.backend_dashboard.backend_dashboard.common.advice;

import com.backend_dashboard.backend_dashboard.common.domain.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.common.exception.CustomException;
import com.backend_dashboard.backend_dashboard.common.exception.ErrorCode;
import com.backend_dashboard.backend_dashboard.common.exception.ErrorCodeInterface;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@Slf4j
@Order(value = 1)
@RestControllerAdvice
public class ApiCommonAdvice {
    @ExceptionHandler(CustomException.class)

    public ResponseEntity<ApiResponseDto<String>> handleCustomException2(CustomException e) {
        ErrorCodeInterface errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus()) // HTTP status
                .body(ApiResponseDto.createError(
                        errorCode.getCode(),       // "COMMON404" 같은 에러코드
                        errorCode.getMessage()     // 메시지
                ));
    }

    // 400 Bad Request : 잘못된 인자
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDto.createError(
                        ErrorCode.INVALID_INPUT.getCode(),
                        e.getMessage()
                ));
    }

    // 404 Not Found : JPA Entity를 못 찾았을 때
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDto<String>> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDto.createError(
                        ErrorCode.NOT_FOUND.getCode(),
                        e.getMessage()
                ));
    }

    // 403 Forbidden : 권한 없을 때
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<String>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDto.createError(
                        ErrorCode.FORBIDDEN.getCode(),
                        e.getMessage()
                ));
    }

    // 500 Internal Server Error : 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<String>> handleGenericException(Exception e) {
        // 로그를 남기고, 메시지는 노출하지 않거나 공통 메시지로 대체
        log.error("Unhandled exception:", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDto.createError(
                        ErrorCode.INTERNAL_ERROR.getCode(),
                        ErrorCode.INTERNAL_ERROR.getMessage()
                ));
    }
}

