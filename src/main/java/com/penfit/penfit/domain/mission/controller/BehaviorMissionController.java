package com.penfit.penfit.domain.mission.controller;

import com.penfit.penfit.domain.mission.dto.BehaviorMissionResponse;
import com.penfit.penfit.domain.mission.dto.MissionCompletionResponse;
import com.penfit.penfit.domain.mission.service.SpendingMissionService;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "행동 미션")
@RestController
@RequestMapping("/api/v1/users/me/behavior-missions")
@RequiredArgsConstructor
public class BehaviorMissionController {

    private final SpendingMissionService spendingMissionService;

    @Operation(summary = "현재 행동 미션 조회",
            description = "남은 기한은 서버가 계산해 daysLeft 로 내려준다. 마감이 지나면 EXPIRED 로 반환한다.")
    @GetMapping("/current")
    public ApiResTemplate<BehaviorMissionResponse> getCurrentMission(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.OK, spendingMissionService.getCurrentMission(userId));
    }

    @Operation(summary = "행동 미션 시작", description = "이미 시작한 미션이면 BM4091 을 반환한다.")
    @PostMapping("/{missionId}/start")
    public ApiResTemplate<BehaviorMissionResponse> start(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long missionId) {
        return ApiResTemplate.success(SuccessCode.OK, spendingMissionService.start(userId, missionId));
    }

    @Operation(summary = "행동 미션 완료",
            description = "목표 금액을 월 납입액으로 보고 예상 연금자산 증가분을 계산해 함께 반환한다. "
                    + "이미 완료한 미션이면 BM4092 를 반환한다.")
    @PostMapping("/{missionId}/complete")
    public ApiResTemplate<BehaviorMissionResponse> complete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long missionId) {
        return ApiResTemplate.success(SuccessCode.OK, spendingMissionService.complete(userId, missionId));
    }

    @Operation(summary = "행동 미션 완료 이력 조회", description = "연도를 생략하면 올해를 조회한다.")
    @GetMapping("/completions")
    public ApiResTemplate<MissionCompletionResponse> getCompletions(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Integer year) {
        int target = year == null ? LocalDate.now().getYear() : year;
        return ApiResTemplate.success(SuccessCode.OK,
                spendingMissionService.getCompletions(userId, target));
    }
}
