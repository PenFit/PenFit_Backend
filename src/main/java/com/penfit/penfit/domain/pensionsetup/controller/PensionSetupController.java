package com.penfit.penfit.domain.pensionsetup.controller;

import com.penfit.penfit.domain.pensionsetup.dto.PensionSetupCreateRequest;
import com.penfit.penfit.domain.pensionsetup.dto.PensionSetupResponse;
import com.penfit.penfit.domain.pensionsetup.service.PensionSetupService;
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

@Tag(name = "가상 연금 설정")
@RestController
@RequestMapping("/api/v1/users/me/pension-setup")
@RequiredArgsConstructor
public class PensionSetupController {

    private final PensionSetupService pensionSetupService;

    @Operation(summary = "내 가상 연금 설정 조회", description = "미등록이면 PS4041 을 반환한다.")
    @GetMapping
    public ApiResTemplate<PensionSetupResponse> getMySetup(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.OK, pensionSetupService.getMySetup(userId));
    }

    @Operation(summary = "가상 연금계좌와 월 납입액 최초 등록",
            description = "30년간 동일한 금액을 납입한다고 가정한 예상 연금자산을 함께 반환한다. 재등록은 PS4091 이다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResTemplate<PensionSetupResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PensionSetupCreateRequest request) {
        return ApiResTemplate.success(SuccessCode.CREATED, pensionSetupService.create(userId, request));
    }
}
