package com.penfit.penfit.domain.user.controller;

import com.penfit.penfit.domain.user.dto.EmailConsentUpdateRequest;
import com.penfit.penfit.domain.user.dto.EmailUpdateRequest;
import com.penfit.penfit.domain.user.dto.NicknameUpdateRequest;
import com.penfit.penfit.domain.user.dto.UserInfoResponse;
import com.penfit.penfit.domain.user.service.UserService;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자")
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 회원정보 조회")
    @GetMapping
    public ApiResTemplate<UserInfoResponse> getMyInfo(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.OK, userService.getMyInfo(userId));
    }

    @Operation(summary = "닉네임 수정")
    @PatchMapping("/nickname")
    public ApiResTemplate<UserInfoResponse> updateNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NicknameUpdateRequest request) {
        return ApiResTemplate.success(SuccessCode.OK, userService.updateNickname(userId, request.nickname()));
    }

    @Operation(summary = "이메일 등록 및 수정", description = "이메일을 등록해도 수신 동의는 활성화되지 않는다.")
    @PutMapping("/email")
    public ApiResTemplate<UserInfoResponse> updateEmail(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody EmailUpdateRequest request) {
        return ApiResTemplate.success(SuccessCode.OK, userService.updateEmail(userId, request.email()));
    }

    @Operation(summary = "등록 이메일 삭제", description = "이미 이메일이 없어도 성공을 반환하는 멱등 API 다.")
    @DeleteMapping("/email")
    public ApiResTemplate<Void> deleteEmail(@AuthenticationPrincipal Long userId) {
        userService.deleteEmail(userId);
        return ApiResTemplate.success(SuccessCode.OK);
    }

    @Operation(summary = "행동 미션 이메일 수신 동의 변경", description = "동의를 켜려면 등록된 이메일이 있어야 한다.")
    @PatchMapping("/email-consent")
    public ApiResTemplate<UserInfoResponse> updateEmailConsent(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody EmailConsentUpdateRequest request) {
        return ApiResTemplate.success(SuccessCode.OK,
                userService.updateEmailConsent(userId, request.emailConsent()));
    }
}
