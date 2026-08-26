package com.penfit.penfit.domain.user.entity;

import com.penfit.penfit.global.common.BaseTimeEntity;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kakao_id", nullable = false, unique = true, length = 50)
    private String kakaoId;

    @Column(nullable = false, length = 30)
    private String nickname;

    @Column(length = 255)
    private String email;

    @Column(name = "email_consent", nullable = false)
    private boolean emailConsent;

    @Column(name = "is_demo", nullable = false)
    private boolean demo;

    @Builder
    private User(String kakaoId, String nickname) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.emailConsent = false;
        this.demo = false;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void removeEmail() {
        this.email = null;
        this.emailConsent = false;
    }

    public void changeEmailConsent(boolean consent) {
        if (consent && this.email == null) {
            throw new BusinessException(ErrorCode.EMAIL_REQUIRED_FOR_CONSENT);
        }
        this.emailConsent = consent;
    }

    public boolean hasEmail() {
        return this.email != null;
    }
}
