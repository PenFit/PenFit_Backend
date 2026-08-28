package com.penfit.penfit.domain.pensionplan.controller;

import com.penfit.penfit.domain.pensionplan.dto.PensionPlanResponse;
import com.penfit.penfit.domain.pensionplan.service.PensionPlanService;
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

@Tag(name = "연금계획")
@RestController
@RequestMapping("/api/v1/users/me/pension-plan")
@RequiredArgsConstructor
public class PensionPlanController {

    private final PensionPlanService pensionPlanService;

    @Operation(summary = "AI 맞춤 연금계획 생성",
            description = "AI 호출을 포함하는 동기식 요청이라 응답까지 시간이 걸린다. "
                    + "실패하면 저장하지 않으므로 같은 API 를 다시 호출하면 된다. "
                    + "유지 가능액이 0원이면 PN4221 을 반환하고 계획을 만들지 않는다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResTemplate<PensionPlanResponse> create(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.CREATED, pensionPlanService.create(userId));
    }

    @Operation(summary = "내 연금계획 조회", description = "사용자당 하나만 존재한다.")
    @GetMapping
    public ApiResTemplate<PensionPlanResponse> getMyPlan(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.OK, pensionPlanService.getMyPlan(userId));
    }
}
