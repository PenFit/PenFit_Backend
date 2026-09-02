package com.penfit.penfit.global.client.ai;

import com.penfit.penfit.global.error.ErrorCode;
import lombok.Getter;

@Getter
public class AiServerException extends RuntimeException {

    private final int httpStatus;
    private final String aiErrorCode;
    private final ErrorCode errorCode;
    private final String responseBody;

    public AiServerException(int httpStatus, String aiErrorCode, ErrorCode errorCode,
                             String message, String responseBody) {
        super(message);
        this.httpStatus = httpStatus;
        this.aiErrorCode = aiErrorCode;
        this.errorCode = errorCode;
        this.responseBody = responseBody;
    }
}
