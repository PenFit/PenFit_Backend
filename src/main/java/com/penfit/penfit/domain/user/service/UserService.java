package com.penfit.penfit.domain.user.service;

import com.penfit.penfit.domain.user.dto.UserInfoResponse;
import com.penfit.penfit.domain.user.entity.User;
import com.penfit.penfit.domain.user.repository.UserRepository;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(Long userId) {
        return UserInfoResponse.from(findUser(userId));
    }

    @Transactional
    public UserInfoResponse updateNickname(Long userId, String nickname) {
        User user = findUser(userId);
        user.changeNickname(nickname.trim());
        return UserInfoResponse.from(user);
    }

    @Transactional
    public UserInfoResponse updateEmail(Long userId, String email) {
        User user = findUser(userId);
        user.changeEmail(email.trim());
        return UserInfoResponse.from(user);
    }

    @Transactional
    public void deleteEmail(Long userId) {
        findUser(userId).removeEmail();
    }

    @Transactional
    public UserInfoResponse updateEmailConsent(Long userId, boolean consent) {
        User user = findUser(userId);
        user.changeEmailConsent(consent);
        return UserInfoResponse.from(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
