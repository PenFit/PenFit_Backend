package com.penfit.penfit.domain.rehearsal.controller;

import com.penfit.penfit.domain.rehearsal.dto.AnswerSaveRequest;
import com.penfit.penfit.domain.rehearsal.dto.RehearsalDetailResponse;
import com.penfit.penfit.domain.rehearsal.dto.RehearsalStartResponse;
import com.penfit.penfit.domain.rehearsal.dto.ScenarioResponse;
import com.penfit.penfit.domain.rehearsal.service.RehearsalService;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import com.penfit.penfit.global.enums.ScenarioCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Tag(name = "연금 리허설")
@RestController
@RequiredArgsConstructor
public class RehearsalController {

    private final RehearsalService rehearsalService;

    @Operation(summary = "연금 리허설 시작",
            description = "금융정보와 가상 연금 설정이 모두 등록되어 있어야 한다. 진행 중 상태로 생성한다.")
    @PostMapping("/api/v1/users/me/rehearsals")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResTemplate<RehearsalStartResponse> start(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.CREATED, rehearsalService.start(userId));
    }

    @Operation(summary = "리허설 상황 6개 조회",
            description = "정해진 순서로 반환한다. IRP 계좌 사용자에게만 중도인출 안내가 함께 내려간다.")
    @GetMapping("/api/v1/rehearsals/{rehearsalId}/scenarios")
    public ApiResTemplate<List<ScenarioResponse>> getScenarios(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long rehearsalId) {
        return ApiResTemplate.success(SuccessCode.OK, rehearsalService.getScenarios(userId, rehearsalId));
    }

    @Operation(summary = "리허설 답변 저장",
            description = "같은 상황의 답변이 이미 있으면 RH4091 을 반환한다.")
    @PostMapping("/api/v1/rehearsals/{rehearsalId}/answers/{scenarioCode}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResTemplate<RehearsalDetailResponse> saveAnswer(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long rehearsalId,
            @PathVariable ScenarioCode scenarioCode,
            @Valid @RequestBody AnswerSaveRequest request) {
        return ApiResTemplate.success(SuccessCode.CREATED,
                rehearsalService.saveAnswer(userId, rehearsalId, scenarioCode, request.optionCode()));
    }

    @Operation(summary = "리허설 진행 상태와 저장된 답변 조회",
            description = "화면 복귀와 새로고침, AI 분석 대기 중 폴링에 사용한다.")
    @GetMapping("/api/v1/rehearsals/{rehearsalId}")
    public ApiResTemplate<RehearsalDetailResponse> getDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long rehearsalId) {
        return ApiResTemplate.success(SuccessCode.OK, rehearsalService.getDetail(userId, rehearsalId));
    }
}
