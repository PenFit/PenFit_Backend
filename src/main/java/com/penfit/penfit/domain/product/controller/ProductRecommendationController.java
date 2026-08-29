package com.penfit.penfit.domain.product.controller;

import com.penfit.penfit.domain.product.dto.ProductComparisonResponse;
import com.penfit.penfit.domain.product.dto.ProductRecommendationResponse;
import com.penfit.penfit.domain.product.service.ProductRecommendationService;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "상품 추천")
@RestController
@RequestMapping("/api/v1/users/me/product-recommendations")
@RequiredArgsConstructor
public class ProductRecommendationController {

    private final ProductRecommendationService productRecommendationService;

    @Operation(summary = "맞춤 연금 상품 3개 추천 생성",
            description = "연금계획의 계좌 종류에 맞는 상품 후보를 AI 에 전달해 3개를 고른다. "
                    + "다시 호출하면 기존 추천을 새 결과로 바꾼다. "
                    + "후보가 3개 미만이면 PR4221, AI 가 3개를 만들지 못하면 PR4222 를 반환한다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResTemplate<List<ProductRecommendationResponse>> create(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.CREATED, productRecommendationService.create(userId));
    }

    @Operation(summary = "내 추천 상품 목록 조회", description = "순위 순서로 3개를 반환한다.")
    @GetMapping
    public ApiResTemplate<List<ProductRecommendationResponse>> getMyRecommendations(
            @AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.OK,
                productRecommendationService.getMyRecommendations(userId));
    }

    @Operation(summary = "추천 상품 비교",
            description = "수수료와 투자상품 선택 범위는 상품 DB 에서, 적합도는 AI 결과에서 가져온다.")
    @GetMapping("/comparison")
    public ApiResTemplate<ProductComparisonResponse> getComparison(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.OK, productRecommendationService.getComparison(userId));
    }
}
