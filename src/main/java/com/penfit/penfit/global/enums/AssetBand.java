package com.penfit.penfit.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssetBand implements DisplayNamed {

    ASSET_LT_10M("1,000만원 미만"),
    ASSET_10M_30M("1,000~3,000만원"),
    ASSET_30M_50M("3,000~5,000만원"),
    ASSET_GE_50M("5,000만원 이상");

    private final String displayName;
}
