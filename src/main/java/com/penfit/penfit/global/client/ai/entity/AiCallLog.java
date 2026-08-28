package com.penfit.penfit.global.client.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "ai_call_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiCallLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "api_name", nullable = false, length = 50)
    private String apiName;

    @Column(name = "request_id", length = 50)
    private String requestId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload", nullable = false)
    private String requestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload")
    private String responsePayload;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "ai_error_code", length = 40)
    private String aiErrorCode;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Builder
    private AiCallLog(Long userId, String apiName, String requestId, String requestPayload,
                      String responsePayload, Integer httpStatus, String aiErrorCode,
                      Integer durationMs, String status) {
        this.userId = userId;
        this.apiName = apiName;
        this.requestId = requestId;
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
        this.httpStatus = httpStatus;
        this.aiErrorCode = aiErrorCode;
        this.durationMs = durationMs;
        this.status = status;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
