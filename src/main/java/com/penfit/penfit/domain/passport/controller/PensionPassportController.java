package com.penfit.penfit.domain.passport.controller;

import com.penfit.penfit.domain.passport.dto.PassportResponse;
import com.penfit.penfit.domain.passport.service.PassportService;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "연금 패스포트")
@RestController
@RequiredArgsConstructor
public class PensionPassportController {

    private final PassportService passportService;

    @Operation(summary = "내 연금 패스포트 조회",
            description = "사용자당 하나만 존재한다. 리허설 분석이 완료되기 전에는 PP4041 을 반환한다.")
    @GetMapping("/api/v1/users/me/pension-passport")
    public ApiResTemplate<PassportResponse> getMyPassport(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.OK, passportService.getMyPassport(userId));
    }
}
