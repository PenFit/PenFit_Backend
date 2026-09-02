package com.penfit.penfit.domain.auth.service;

import com.penfit.penfit.domain.auth.dto.LoginResponse;
import com.penfit.penfit.domain.auth.dto.LoginResult;
import com.penfit.penfit.domain.auth.dto.ReissueResponse;
import com.penfit.penfit.domain.auth.dto.ReissueResult;
import com.penfit.penfit.domain.auth.dto.TokenBundle;
import com.penfit.penfit.domain.auth.entity.RefreshToken;
import com.penfit.penfit.domain.auth.repository.RefreshTokenRepository;
import com.penfit.penfit.domain.user.entity.User;
import com.penfit.penfit.domain.user.repository.UserRepository;
import com.penfit.penfit.global.client.kakao.KakaoOAuthClient;
import com.penfit.penfit.global.client.kakao.KakaoUserInfo;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import com.penfit.penfit.global.config.DemoProperties;
import com.penfit.penfit.global.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEMO_KAKAO_ID_PREFIX = "demo-";

    private final KakaoOAuthClient kakaoOAuthClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final DemoProperties demoProperties;

    @Transactional
    public LoginResult login(String authorizationCode) {
        KakaoUserInfo kakaoUser = kakaoOAuthClient.fetchUserInfo(authorizationCode);

        Optional<User> existing = userRepository.findByKakaoId(kakaoUser.kakaoId());
        boolean newUser = existing.isEmpty();
        User user = existing.orElseGet(() -> userRepository.save(User.builder()
                .kakaoId(kakaoUser.kakaoId())
                .nickname(kakaoUser.nickname())
                .build()));

        TokenBundle tokens = issueTokens(user.getId());
        LoginResponse response = new LoginResponse(
                tokens.accessToken(), user.getId(), user.getNickname(), newUser);

        return new LoginResult(response, tokens);
    }

    @Transactional
    public LoginResult demoLogin() {
        if (!demoProperties.enabled()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        User user = userRepository.save(User.demo(
                DEMO_KAKAO_ID_PREFIX + UUID.randomUUID(), demoProperties.nickname()));

        TokenBundle tokens = issueTokens(user.getId());
        LoginResponse response = new LoginResponse(
                tokens.accessToken(), user.getId(), user.getNickname(), true);

        return new LoginResult(response, tokens);
    }

    @Transactional
    public ReissueResult reissue(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.parseRefreshToken(refreshTokenValue);
        RefreshToken stored = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!stored.matches(refreshTokenValue)) {
            refreshTokenRepository.delete(stored);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }
        if (!userRepository.existsById(userId)) {
            refreshTokenRepository.delete(stored);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String accessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);
        stored.rotate(newRefreshToken, jwtProvider.refreshTokenExpiresAt());

        TokenBundle tokens = new TokenBundle(
                accessToken, newRefreshToken, jwtProvider.refreshTokenMaxAgeSeconds());
        return new ReissueResult(new ReissueResponse(accessToken), tokens);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            return;
        }
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(refreshTokenRepository::delete);
    }

    private TokenBundle issueTokens(Long userId) {
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);

        refreshTokenRepository.findByUserId(userId)
                .ifPresentOrElse(
                        stored -> stored.rotate(refreshToken, jwtProvider.refreshTokenExpiresAt()),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .userId(userId)
                                .token(refreshToken)
                                .expiresAt(jwtProvider.refreshTokenExpiresAt())
                                .build()));

        return new TokenBundle(accessToken, refreshToken, jwtProvider.refreshTokenMaxAgeSeconds());
    }
}
