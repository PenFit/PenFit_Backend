package com.penfit.penfit.global.client.ai;

import com.penfit.penfit.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiClientErrorMappingTest {

    private static final String TPM_BODY = """
            {"detail": "Error code: 429 - {'error': {'message': 'Rate limit reached for model \
            `openai/gpt-oss-120b` on tokens per minute (TPM): Limit 8000, Used 7860, \
            Requested 5274. Please try again in 38.505s.', 'code': 'rate_limit_exceeded'}}"}""";

    private static final String TPD_BODY = """
            {"detail": "Error code: 429 - {'error': {'message': 'Rate limit reached for model \
            `openai/gpt-oss-120b` on tokens per day (TPD): Limit 200000, Used 200000, \
            Requested 5274.', 'code': 'rate_limit_exceeded'}}"}""";

    @Test
    @DisplayName("분당 한도 초과는 재시도 가능한 코드로 구분한다")
    void mapsRateLimit() {
        assertThat(AiClient.toErrorCode(500, null, TPM_BODY)).isEqualTo(ErrorCode.AI_RATE_LIMITED);
    }

    @Test
    @DisplayName("하루 한도 초과는 별도 코드로 구분한다")
    void mapsDailyLimit() {
        assertThat(AiClient.toErrorCode(500, null, TPD_BODY)).isEqualTo(ErrorCode.AI_DAILY_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("한도 문구가 없으면 기존 매핑을 따른다")
    void keepsExistingMapping() {
        assertThat(AiClient.toErrorCode(504, null, "{}")).isEqualTo(ErrorCode.AI_TIMEOUT);
        assertThat(AiClient.toErrorCode(400, null, "{}")).isEqualTo(ErrorCode.AI_INVALID_RESPONSE);
        assertThat(AiClient.toErrorCode(500, null, "{}")).isEqualTo(ErrorCode.AI_SERVER_ERROR);
        assertThat(AiClient.toErrorCode(500, null, null)).isEqualTo(ErrorCode.AI_SERVER_ERROR);
    }

    @Test
    @DisplayName("422 상세 코드 매핑은 그대로 유지한다")
    void keepsUnprocessableMapping() {
        assertThat(AiClient.toErrorCode(422, "INSUFFICIENT_RECOMMENDATIONS", "{}"))
                .isEqualTo(ErrorCode.INSUFFICIENT_RECOMMENDATIONS);
        assertThat(AiClient.toErrorCode(422, "NO_ACTIONABLE_SPENDING", "{}"))
                .isEqualTo(ErrorCode.NO_ACTIONABLE_SPENDING);
        assertThat(AiClient.toErrorCode(422, "UNKNOWN", "{}")).isEqualTo(ErrorCode.AI_ANALYSIS_FAILED);
    }
}
