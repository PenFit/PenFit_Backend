package com.penfit.penfit.domain.rehearsal.dto;

import com.penfit.penfit.global.enums.OptionCode;
import jakarta.validation.constraints.NotNull;

public record AnswerSaveRequest(
        @NotNull(message = "선택지 코드는 필수입니다.")
        OptionCode optionCode
) {
}
