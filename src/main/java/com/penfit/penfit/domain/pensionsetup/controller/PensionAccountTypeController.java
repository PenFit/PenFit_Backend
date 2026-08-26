package com.penfit.penfit.domain.pensionsetup.controller;

import com.penfit.penfit.domain.pensionsetup.dto.PensionAccountTypeResponse;
import com.penfit.penfit.domain.pensionsetup.service.PensionAccountTypeCatalog;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.common.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "가상 연금 설정")
@RestController
@RequestMapping("/api/v1/pension-setups")
@RequiredArgsConstructor
public class PensionAccountTypeController {

    private final PensionAccountTypeCatalog pensionAccountTypeCatalog;

    @Operation(summary = "리허설용 연금계좌 종류 조회",
            description = "계좌 선택 화면과 비교 화면에 표시할 3개 계좌의 정보를 반환한다. 정적 데이터다.")
    @GetMapping("/account-types")
    public ApiResTemplate<List<PensionAccountTypeResponse>> getAccountTypes() {
        return ApiResTemplate.success(SuccessCode.OK, pensionAccountTypeCatalog.findAll());
    }
}
