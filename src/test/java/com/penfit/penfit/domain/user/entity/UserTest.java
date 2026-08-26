package com.penfit.penfit.domain.user.entity;

import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private User newUser() {
        return User.builder().kakaoId("123456").nickname("이재원").build();
    }

    @Test
    @DisplayName("생성 직후에는 이메일이 없고 수신 동의는 꺼져 있다")
    void startsWithoutEmail() {
        User user = newUser();

        assertThat(user.getEmail()).isNull();
        assertThat(user.isEmailConsent()).isFalse();
        assertThat(user.isDemo()).isFalse();
    }

    @Test
    @DisplayName("이메일을 등록해도 수신 동의는 자동으로 켜지지 않는다")
    void registeringEmailDoesNotEnableConsent() {
        User user = newUser();

        user.changeEmail("jaewon@example.com");

        assertThat(user.hasEmail()).isTrue();
        assertThat(user.isEmailConsent()).isFalse();
    }

    @Test
    @DisplayName("이메일이 없으면 수신 동의를 켤 수 없다")
    void cannotEnableConsentWithoutEmail() {
        User user = newUser();

        assertThatThrownBy(() -> user.changeEmailConsent(true))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_REQUIRED_FOR_CONSENT);
    }

    @Test
    @DisplayName("이메일을 삭제하면 수신 동의도 함께 꺼진다")
    void removingEmailDisablesConsent() {
        User user = newUser();
        user.changeEmail("jaewon@example.com");
        user.changeEmailConsent(true);

        user.removeEmail();

        assertThat(user.getEmail()).isNull();
        assertThat(user.isEmailConsent()).isFalse();
    }

    @Test
    @DisplayName("동의를 거부해도 등록된 이메일은 지우지 않는다")
    void decliningConsentKeepsEmail() {
        User user = newUser();
        user.changeEmail("jaewon@example.com");
        user.changeEmailConsent(true);

        user.changeEmailConsent(false);

        assertThat(user.getEmail()).isEqualTo("jaewon@example.com");
        assertThat(user.isEmailConsent()).isFalse();
    }
}
