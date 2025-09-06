package com.backend_dashboard.backend_dashboard.common.advice;

import com.backend_dashboard.backend_dashboard.common.domain.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.common.exception.CustomException;
import com.backend_dashboard.backend_dashboard.common.exception.ErrorCodeInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity
                .badRequest()
                .body(ApiResponseDto.createError("VALIDATION_FAILED", "invalid DTO", errors));
    }

}
