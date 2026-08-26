package com.penfit.penfit.domain.financialprofile.controller;

import com.penfit.penfit.domain.financialprofile.dto.FinancialProfileCreateRequest;
import com.penfit.penfit.domain.financialprofile.dto.FinancialProfileResponse;
import com.penfit.penfit.domain.financialprofile.service.FinancialProfileService;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "금융정보")
@RestController
@RequestMapping("/api/v1/users/me/financial-profile")
@RequiredArgsConstructor
public class FinancialProfileController {

    private final FinancialProfileService financialProfileService;

    @Operation(summary = "내 금융정보 조회",
            description = "아직 입력하지 않은 사용자는 FP4041 을 반환한다. 프론트엔드는 정보 입력 화면으로 이동시킨다.")
    @GetMapping
    public ApiResTemplate<FinancialProfileResponse> getMyProfile(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.OK, financialProfileService.getMyProfile(userId));
    }

    @Operation(summary = "내 금융정보 최초 등록",
            description = "모든 필수 항목을 한 번에 전달한다. 이미 등록된 사용자는 FP4091 을 반환한다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResTemplate<FinancialProfileResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody FinancialProfileCreateRequest request) {
        return ApiResTemplate.success(SuccessCode.CREATED, financialProfileService.create(userId, request));
    }
}
