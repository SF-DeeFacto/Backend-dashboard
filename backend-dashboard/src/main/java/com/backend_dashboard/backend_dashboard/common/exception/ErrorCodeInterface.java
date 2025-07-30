package com.backend_dashboard.backend_dashboard.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCodeInterface {
    String getCode();
    String getMessage();
    HttpStatus getStatus();
}
