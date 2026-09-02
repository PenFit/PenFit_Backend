package com.penfit.penfit.domain.auth.controller;

import com.penfit.penfit.domain.auth.dto.KakaoLoginRequest;
import com.penfit.penfit.domain.auth.dto.LoginResponse;
import com.penfit.penfit.domain.auth.dto.LoginResult;
import com.penfit.penfit.domain.auth.dto.ReissueResponse;
import com.penfit.penfit.domain.auth.dto.ReissueResult;
import com.penfit.penfit.domain.auth.dto.TokenBundle;
import com.penfit.penfit.domain.auth.service.AuthService;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import com.penfit.penfit.global.security.RefreshTokenCookieFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieFactory cookieFactory;

    @Operation(summary = "카카오 로그인", description = "프론트엔드가 전달한 인가 코드로 로그인하고 토큰을 발급한다.")
    @PostMapping("/kakao/login")
    public ResponseEntity<ApiResTemplate<LoginResponse>> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request) {
        LoginResult result = authService.login(request.code());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(result.tokens()))
                .body(ApiResTemplate.success(SuccessCode.OK, result.response()));
    }

    @Operation(summary = "심사용 데모 로그인",
            description = "카카오 없이 새 데모 계정을 만들고 토큰을 발급한다. "
                    + "누를 때마다 독립된 계정이 생겨 여러 사람이 동시에 체험할 수 있다. "
                    + "기능이 꺼져 있으면 404 를 반환한다.")
    @PostMapping("/demo-login")
    public ResponseEntity<ApiResTemplate<LoginResponse>> demoLogin() {
        LoginResult result = authService.demoLogin();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(result.tokens()))
                .body(ApiResTemplate.success(SuccessCode.OK, result.response()));
    }

    @Operation(summary = "Access Token 재발급", description = "쿠키의 Refresh Token 을 검증하고 새 Access Token 을 발급한다.")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResTemplate<ReissueResponse>> reissue(HttpServletRequest request) {
        ReissueResult result = authService.reissue(cookieFactory.readToken(request));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie(result.tokens()))
                .body(ApiResTemplate.success(SuccessCode.OK, result.response()));
    }

    @Operation(summary = "로그아웃", description = "저장된 Refresh Token 을 삭제하고 쿠키를 만료시킨다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResTemplate<Void>> logout(HttpServletRequest request) {
        authService.logout(cookieFactory.readToken(request));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.expired().toString())
                .body(ApiResTemplate.success(SuccessCode.OK));
    }

    private String refreshCookie(TokenBundle tokens) {
        return cookieFactory.create(tokens.refreshToken(), tokens.refreshTokenMaxAgeSeconds()).toString();
    }
}
