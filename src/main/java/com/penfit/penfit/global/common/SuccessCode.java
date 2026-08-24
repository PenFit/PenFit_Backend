package com.penfit.penfit.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 성공 응답 코드. 에러 코드와 같은 {도메인2}{HTTP상태3}{일련1} 규칙을 따른다.
 */
@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    OK(HttpStatus.OK, "CM2001", "요청이 성공했습니다."),
    CREATED(HttpStatus.CREATED, "CM2011", "생성되었습니다."),
    ACCEPTED(HttpStatus.ACCEPTED, "CM2021", "요청이 접수되었습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "CM2041", "삭제되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
