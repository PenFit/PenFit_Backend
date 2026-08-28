package com.penfit.penfit.global.client.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.global.client.ai.dto.PassportAnalyzeResponse;
import com.penfit.penfit.global.config.AiProperties;
import com.penfit.penfit.global.error.BusinessException;
import com.penfit.penfit.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiClientTest {

    private static final String UNUSED_PORT_URL = "http://127.0.0.1:1";

    @Test
    @DisplayName("AI 서버에 연결하지 못하면 시간 초과가 아니라 서버 오류로 처리한다")
    void distinguishesConnectFailureFromTimeout() {
        AiClient client = clientOf(UNUSED_PORT_URL);

        assertThatThrownBy(() -> client.call(AiApi.PASSPORT, "{}", PassportAnalyzeResponse.class, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.AI_SERVER_ERROR);
    }

    @Test
    @DisplayName("연결 실패는 CONNECT_FAILED 로 기록한다")
    void logsConnectFailure() {
        AiCallLogger logger = Mockito.mock(AiCallLogger.class);
        AiClient client = new AiClient(propertiesOf(UNUSED_PORT_URL), logger, new ObjectMapper());

        assertThatThrownBy(() -> client.call(AiApi.PASSPORT, "{}", PassportAnalyzeResponse.class, 1L))
                .isInstanceOf(BusinessException.class);

        Mockito.verify(logger).failure(
                Mockito.eq(1L), Mockito.eq(AiApi.PASSPORT), Mockito.any(),
                Mockito.isNull(), Mockito.eq("CONNECT_FAILED"), Mockito.anyLong());
    }

    @Test
    @DisplayName("API 마다 정해진 경로를 사용한다")
    void usesFixedPaths() {
        assertThat(AiApi.PASSPORT.getPath()).isEqualTo("/internal/v1/pension-passport/analyze");
        assertThat(AiApi.PENSION_PLAN.getPath()).isEqualTo("/internal/v1/pension-plan/generate");
        assertThat(AiApi.PRODUCT_RECOMMENDATION.getPath())
                .isEqualTo("/internal/v1/product-recommendations/generate");
        assertThat(AiApi.SPENDING_MISSION.getPath()).isEqualTo("/internal/v1/spending-mission/analyze");
    }

    private AiClient clientOf(String baseUrl) {
        return new AiClient(propertiesOf(baseUrl), Mockito.mock(AiCallLogger.class), new ObjectMapper());
    }

    private AiProperties propertiesOf(String baseUrl) {
        return new AiProperties(baseUrl, "test-key", Duration.ofSeconds(1),
                new AiProperties.Timeout(Duration.ofSeconds(2), Duration.ofSeconds(2),
                        Duration.ofSeconds(2), Duration.ofSeconds(2)));
    }
}
