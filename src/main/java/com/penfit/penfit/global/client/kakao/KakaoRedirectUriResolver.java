package com.penfit.penfit.global.client.kakao;

import com.penfit.penfit.global.config.KakaoProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KakaoRedirectUriResolver {

    private final List<String> redirectUris;

    public KakaoRedirectUriResolver(KakaoProperties properties) {
        List<String> configured = properties.redirectUris();
        if (configured == null || configured.isEmpty()) {
            throw new IllegalStateException("penfit.kakao.redirect-uris 가 비어 있습니다");
        }
        this.redirectUris = List.copyOf(configured);
    }

    public String resolve(String origin) {
        if (origin == null || origin.isBlank()) {
            return defaultUri();
        }
        return redirectUris.stream()
                .filter(uri -> uri.startsWith(origin + "/"))
                .findFirst()
                .orElseGet(this::defaultUri);
    }

    public String defaultUri() {
        return redirectUris.get(0);
    }
}
