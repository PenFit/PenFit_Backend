package com.penfit.penfit.global.client.ai;

import com.penfit.penfit.global.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class AiHealthPinger {

    private static final String HEALTH_PATH = "/health";
    private static final int TIMEOUT_MILLIS = 5000;

    private final RestClient restClient;

    public AiHealthPinger(AiProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MILLIS);
        factory.setReadTimeout(TIMEOUT_MILLIS);

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }

    @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
    public void keepAwake() {
        try {
            restClient.get().uri(HEALTH_PATH).retrieve().toBodilessEntity();
        } catch (RuntimeException e) {
            log.debug("AI 서버 상태 확인 실패", e);
        }
    }
}
