package com.backend_dashboard.backend_dashboard.common.exception;

public class CustomException extends RuntimeException {
    private final ErrorCodeInterface errorCode;

    public CustomException(ErrorCodeInterface errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCodeInterface getErrorCode() {
        return errorCode;
    }
}
