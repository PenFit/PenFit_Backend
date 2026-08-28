package com.penfit.penfit.domain.product.controller;

import com.penfit.penfit.domain.product.dto.ProductDetailResponse;
import com.penfit.penfit.domain.product.service.ProductService;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "연금 상품")
@RestController
@RequiredArgsConstructor
public class PensionProductController {

    private final ProductService productService;

    @Operation(summary = "연금 상품 상세 조회",
            description = "추천을 아직 받지 않았다면 recommendationReason 은 null 로 내려간다. "
                    + "saved 는 요청한 사용자가 이 상품을 담아뒀는지를 뜻한다.")
    @GetMapping("/api/v1/pension-products/{productId}")
    public ApiResTemplate<ProductDetailResponse> getDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long productId) {
        return ApiResTemplate.success(SuccessCode.OK, productService.getDetail(userId, productId));
    }
}
