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
                        e.getMessage()     // 메시지
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<String>> handleException(Exception e) {
        log.error("Unhandled exception: ", e);
        return ResponseEntity
                .status(500)
                .body(ApiResponseDto.createError("COMMON500", "Internal Server Error"));
    }
}

