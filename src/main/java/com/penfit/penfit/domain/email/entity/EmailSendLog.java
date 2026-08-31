package com.penfit.penfit.domain.email.entity;

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

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "email_send_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailSendLog {

    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";
    private static final int MESSAGE_LIMIT = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "mission_id")
    private Long missionId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "sent_at", nullable = false)
    private OffsetDateTime sentAt;

    @Builder
    private EmailSendLog(Long userId, Long missionId, String email, String subject,
                         String status, String errorMessage) {
        this.userId = userId;
        this.missionId = missionId;
        this.email = email;
        this.subject = subject;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public static EmailSendLog success(Long userId, Long missionId, String email, String subject) {
        return EmailSendLog.builder()
                .userId(userId)
                .missionId(missionId)
                .email(email)
                .subject(subject)
                .status(SUCCESS)
                .build();
    }

    public static EmailSendLog failed(Long userId, Long missionId, String email, String subject,
                                      String errorMessage) {
        return EmailSendLog.builder()
                .userId(userId)
                .missionId(missionId)
                .email(email)
                .subject(subject)
                .status(FAILED)
                .errorMessage(errorMessage == null ? null
                        : errorMessage.substring(0, Math.min(errorMessage.length(), MESSAGE_LIMIT)))
                .build();
    }

    @PrePersist
    void onCreate() {
        this.sentAt = OffsetDateTime.now();
    }
}
