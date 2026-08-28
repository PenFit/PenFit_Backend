package com.penfit.penfit.domain.product.controller;

import com.penfit.penfit.domain.product.dto.SavedProductResponse;
import com.penfit.penfit.domain.product.service.SavedProductService;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "연금 상품")
@RestController
@RequestMapping("/api/v1/users/me/saved-products")
@RequiredArgsConstructor
public class SavedProductController {

    private final SavedProductService savedProductService;

    @Operation(summary = "상품 담아두기", description = "이미 담아둔 상품이면 PR4091 을 반환한다.")
    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResTemplate<Void> save(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId) {
        savedProductService.save(userId, productId);
        return ApiResTemplate.success(SuccessCode.CREATED);
    }

    @Operation(summary = "내가 담은 상품 목록 조회", description = "최근에 담은 순서로 반환한다.")
    @GetMapping
    public ApiResTemplate<List<SavedProductResponse>> getMySavedProducts(@AuthenticationPrincipal Long userId) {
        return ApiResTemplate.success(SuccessCode.OK, savedProductService.getMySavedProducts(userId));
    }

    @Operation(summary = "담아둔 상품 취소", description = "담아두지 않은 상품이어도 성공을 반환한다.")
    @DeleteMapping("/{productId}")
    public ApiResTemplate<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId) {
        savedProductService.delete(userId, productId);
        return ApiResTemplate.success(SuccessCode.OK);
    }
}
