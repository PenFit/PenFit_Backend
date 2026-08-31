package com.penfit.penfit.domain.home.controller;

import com.penfit.penfit.domain.home.dto.HomeResponse;
import com.penfit.penfit.domain.home.service.HomeService;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "메인")
@RestController
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @Operation(summary = "메인페이지 조회",
            description = "패스포트와 연금계획, 이번 주 미션, 담아둔 상품을 한 번에 반환한다. "
                    + "아직 만들지 않은 항목은 null 로 내려간다.")
    @GetMapping("/api/v1/users/me/home")
    public ApiResTemplate<HomeResponse> getHome(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.OK, homeService.getHome(userId));
    }
}
