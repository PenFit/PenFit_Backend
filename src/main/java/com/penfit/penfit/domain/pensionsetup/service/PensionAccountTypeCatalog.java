package com.penfit.penfit.domain.pensionsetup.service;

import com.penfit.penfit.domain.pensionsetup.dto.PensionAccountTypeResponse;
import com.penfit.penfit.domain.pensionsetup.dto.PensionAccountTypeResponse.Comparison;
import com.penfit.penfit.global.enums.AccountType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PensionAccountTypeCatalog {

    private static final List<PensionAccountTypeResponse> ACCOUNT_TYPES = List.of(
            new PensionAccountTypeResponse(
                    AccountType.PENSION_SAVINGS_FUND.name(),
                    AccountType.PENSION_SAVINGS_FUND.getDisplayName(),
                    "직접 굴리는 연금",
                    "ETF와 펀드를 직접 선택해 투자해요",
                    List.of("#세제혜택가능", "#자산배분전략", "#장기투자효과", "#직접운용"),
                    new Comparison(
                            "ETF·펀드를 직접 골라 투자",
                            "연 최대 600만원 (IRP 합산 시 900만원)",
                            "자산 배분을 자유롭게 조절",
                            "투자를 직접 해보고 싶은 분")),
            new PensionAccountTypeResponse(
                    AccountType.INDIVIDUAL_IRP.name(),
                    AccountType.INDIVIDUAL_IRP.getDisplayName(),
                    "퇴직금과 함께 모으는 연금",
                    "퇴직금과 개인 납입금을 한 계좌에서 관리해요",
                    List.of("#퇴직연금전용", "#자산관리효율성", "#퇴직금연계", "#세제혜택가능"),
                    new Comparison(
                            "퇴직금 + 개인 납입을 한 계좌로 관리",
                            "연 최대 900만원 + 퇴직금 이연 과세",
                            "퇴직금 관리 효율성이 높음",
                            "퇴직금을 한 곳에 모아 보고 싶은 분")),
            new PensionAccountTypeResponse(
                    AccountType.PENSION_SAVINGS_INSURANCE.name(),
                    AccountType.PENSION_SAVINGS_INSURANCE.getDisplayName(),
                    "꾸준히 받는 보험형 연금",
                    "정해진 방식에 따라 매달 꾸준히 쌓아가요",
                    List.of("#정기납입", "#안정성중심", "#안정적인수익률", "#보장형상품"),
                    new Comparison(
                            "정해진 방식으로 매달 꾸준히 납입",
                            "연 최대 600만원 (IRP 합산 시 900만원)",
                            "안정적인 수익률에 중점",
                            "꾸준하고 안전하게 모으고 싶은 분")));

    private static final Map<String, PensionAccountTypeResponse> BY_CODE = ACCOUNT_TYPES.stream()
            .collect(Collectors.toMap(PensionAccountTypeResponse::code, Function.identity()));

    public List<PensionAccountTypeResponse> findAll() {
        return ACCOUNT_TYPES;
    }

    public PensionAccountTypeResponse findByCode(AccountType accountType) {
        return BY_CODE.get(accountType.name());
    }
}
