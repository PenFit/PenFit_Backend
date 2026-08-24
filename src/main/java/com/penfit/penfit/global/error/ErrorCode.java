package com.penfit.penfit.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "CM4001", "요청 값이 올바르지 않습니다."),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "CM4002", "요청 본문을 읽을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "CM4011", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "CM4031", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "CM4041", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "CM4051", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CM5001", "서버 오류가 발생했습니다."),

    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AU4011", "유효하지 않은 액세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AU4012", "만료된 액세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AU4013", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AU4014", "만료된 리프레시 토큰입니다. 다시 로그인해주세요."),
    KAKAO_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "AU4015", "카카오 인증에 실패했습니다."),
    KAKAO_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "AU5021", "카카오 서버와 통신하지 못했습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "US4041", "사용자를 찾을 수 없습니다."),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "US4001", "닉네임 형식이 올바르지 않습니다."),
    EMAIL_REQUIRED_FOR_CONSENT(HttpStatus.CONFLICT, "US4091", "등록된 이메일이 없어 수신 동의를 활성화할 수 없습니다."),

    FINANCIAL_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FP4041", "등록된 금융정보가 없습니다."),
    FINANCIAL_PROFILE_ALREADY_EXISTS(HttpStatus.CONFLICT, "FP4091", "이미 금융정보가 등록되어 있습니다."),

    PENSION_SETUP_NOT_FOUND(HttpStatus.NOT_FOUND, "PS4041", "가상 연금 설정이 없습니다."),
    PENSION_SETUP_ALREADY_EXISTS(HttpStatus.CONFLICT, "PS4091", "이미 가상 연금 설정이 등록되어 있습니다."),
    INVALID_MONTHLY_CONTRIBUTION(HttpStatus.BAD_REQUEST, "PS4001", "월 납입액은 5만원 이상이어야 합니다."),

    REHEARSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "RH4041", "리허설을 찾을 수 없습니다."),
    REHEARSAL_FORBIDDEN(HttpStatus.FORBIDDEN, "RH4031", "본인의 리허설이 아닙니다."),
    INVALID_SCENARIO_OPTION(HttpStatus.BAD_REQUEST, "RH4001", "허용되지 않은 상황과 선택지 조합입니다."),
    ANSWER_ALREADY_EXISTS(HttpStatus.CONFLICT, "RH4091", "이미 답변한 상황입니다."),
    REHEARSAL_NOT_IN_PROGRESS(HttpStatus.CONFLICT, "RH4092", "진행 중인 리허설이 아닙니다."),
    REHEARSAL_ANSWERS_INCOMPLETE(HttpStatus.CONFLICT, "RH4093", "6개 상황의 답변이 모두 필요합니다."),
    REHEARSAL_RETRY_NOT_ALLOWED(HttpStatus.CONFLICT, "RH4094", "분석에 실패한 리허설만 재시도할 수 있습니다."),

    PASSPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "PP4041", "연금 패스포트가 없습니다."),

    PENSION_PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "PN4041", "저장된 연금 계획이 없습니다."),
    PENSION_PLAN_ALREADY_EXISTS(HttpStatus.CONFLICT, "PN4092", "이미 연금 계획이 존재합니다."),
    NO_SUSTAINABLE_CONTRIBUTION(HttpStatus.UNPROCESSABLE_ENTITY, "PN4221",
            "현재는 연금 납입보다 비상금과 현금흐름 확보가 먼저예요."),

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR4041", "연금 상품을 찾을 수 없습니다."),
    RECOMMENDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PR4042", "추천 결과가 없습니다."),
    SAVED_PRODUCT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PR4091", "이미 담아둔 상품입니다."),
    INSUFFICIENT_PRODUCT_CANDIDATES(HttpStatus.UNPROCESSABLE_ENTITY, "PR4221",
            "조건에 맞는 상품 후보가 부족해요. 금융회사 공식 정보를 확인해주세요."),
    INSUFFICIENT_RECOMMENDATIONS(HttpStatus.UNPROCESSABLE_ENTITY, "PR4222",
            "추천 가능한 상품 3개를 만들지 못했어요."),

    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "BM4041", "진행 중인 미션이 없습니다."),
    SPENDING_ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "BM4042", "소비 분석 결과가 없습니다."),
    MISSION_ALREADY_STARTED(HttpStatus.CONFLICT, "BM4091", "이미 시작한 미션입니다."),
    MISSION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "BM4092", "이미 완료한 미션입니다."),
    NO_ACTIONABLE_SPENDING(HttpStatus.UNPROCESSABLE_ENTITY, "BM4221",
            "미션으로 만들 만한 절감 가능액이 없어요."),

    AI_ANALYSIS_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "AI4221", "AI 분석에 실패했어요. 다시 시도해주세요."),
    AI_INVALID_RESPONSE(HttpStatus.BAD_GATEWAY, "AI5021", "AI 서버 응답이 올바르지 않습니다."),
    AI_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "AI5022", "AI 서버 오류가 발생했습니다."),
    AI_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI5041", "AI 분석 시간이 초과됐어요. 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
