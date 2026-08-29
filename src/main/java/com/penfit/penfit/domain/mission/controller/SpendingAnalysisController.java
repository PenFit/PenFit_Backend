package com.penfit.penfit.domain.mission.controller;

import com.penfit.penfit.domain.mission.dto.SpendingAnalysisResponse;
import com.penfit.penfit.domain.mission.service.SpendingMissionService;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "행동 미션")
@RestController
@RequestMapping("/api/v1/users/me/spending-analysis")
@RequiredArgsConstructor
public class SpendingAnalysisController {

    private final SpendingMissionService spendingMissionService;

    @Operation(summary = "가상 소비내역 분석과 행동 미션 생성",
            description = "가상 거래내역을 AI 에 전달해 소비 분석과 이번 주 미션을 함께 만든다. "
                    + "AI 호출을 포함하는 동기식 요청이다. "
                    + "절감할 수 있는 지출이 없으면 BM4221 을 반환한다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResTemplate<SpendingAnalysisResponse> analyze(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.CREATED, spendingMissionService.analyze(userId));
    }

    @Operation(summary = "내 소비 분석 결과 조회",
            description = "영역별 지출은 지출이 없는 영역까지 포함해 다섯 개를 반환한다.")
    @GetMapping
    public ApiResTemplate<SpendingAnalysisResponse> getMyAnalysis(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.OK, spendingMissionService.getMyAnalysis(userId));
    }
}
