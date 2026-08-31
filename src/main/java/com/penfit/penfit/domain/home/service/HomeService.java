package com.penfit.penfit.domain.home.service;

import com.penfit.penfit.domain.home.dto.HomeResponse;
import com.penfit.penfit.domain.mission.repository.BehaviorMissionRepository;
import com.penfit.penfit.domain.passport.repository.PensionPassportRepository;
import com.penfit.penfit.domain.pensionplan.repository.PensionPlanRepository;
import com.penfit.penfit.domain.product.entity.PensionProduct;
import com.penfit.penfit.domain.product.entity.SavedProduct;
import com.penfit.penfit.domain.product.repository.PensionProductRepository;
import com.penfit.penfit.domain.product.repository.SavedProductRepository;
import com.penfit.penfit.domain.user.entity.User;
import com.penfit.penfit.domain.user.repository.UserRepository;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final UserRepository userRepository;
    private final PensionPassportRepository pensionPassportRepository;
    private final PensionPlanRepository pensionPlanRepository;
    private final BehaviorMissionRepository behaviorMissionRepository;
    private final SavedProductRepository savedProductRepository;
    private final PensionProductRepository pensionProductRepository;

    @Transactional(readOnly = true)
    public HomeResponse getHome(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new HomeResponse(
                user.getNickname(),
                pensionPassportRepository.findByUserId(userId)
                        .map(HomeResponse.PassportCard::of)
                        .orElse(null),
                pensionPlanRepository.findByUserId(userId)
                        .map(HomeResponse.PensionPlanCard::of)
                        .orElse(null),
                behaviorMissionRepository.findFirstByUserIdOrderByIdDesc(userId)
                        .map(HomeResponse.MissionCard::of)
                        .orElse(null),
                savedProductCards(userId));
    }

    private List<HomeResponse.SavedProductCard> savedProductCards(Long userId) {
        List<SavedProduct> saved = savedProductRepository.findAllByUserIdOrderBySavedAtDesc(userId);
        if (saved.isEmpty()) {
            return List.of();
        }

        Map<Long, PensionProduct> products = pensionProductRepository
                .findAllByIdInAndIsActiveTrue(saved.stream().map(SavedProduct::getProductId).toList())
                .stream()
                .collect(Collectors.toMap(PensionProduct::getId, Function.identity()));

        return saved.stream()
                .filter(item -> products.containsKey(item.getProductId()))
                .map(item -> HomeResponse.SavedProductCard.of(products.get(item.getProductId())))
                .toList();
    }
}
