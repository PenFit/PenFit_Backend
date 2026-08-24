package com.penfit.penfit.global.common;

import com.penfit.penfit.global.error.ErrorCode;
import lombok.Getter;

/**
 * 모든 사용자 API의 공통 응답 포맷. 성공과 실패 모두 {code, message, data} 구조로 감싼다.
 * AI 서버 내부 API는 이 포맷을 쓰지 않고 data JSON만 주고받는다.
 */
@Getter
public class ApiResTemplate<T> {

    private final String code;
    private final String message;
    private final T data;

    private ApiResTemplate(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResTemplate<T> success(SuccessCode successCode, T data) {
        return new ApiResTemplate<>(successCode.getCode(), successCode.getMessage(), data);
    }

    public static <T> ApiResTemplate<T> success(SuccessCode successCode) {
        return new ApiResTemplate<>(successCode.getCode(), successCode.getMessage(), null);
    }

    public static <T> ApiResTemplate<T> error(ErrorCode errorCode) {
        return new ApiResTemplate<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResTemplate<T> error(ErrorCode errorCode, String message) {
        return new ApiResTemplate<>(errorCode.getCode(), message, null);
    }
}
