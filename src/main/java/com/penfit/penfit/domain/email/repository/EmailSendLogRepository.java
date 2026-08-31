package com.penfit.penfit.domain.email.repository;

import com.penfit.penfit.domain.email.entity.EmailSendLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

public interface EmailSendLogRepository extends JpaRepository<EmailSendLog, Long> {

    boolean existsByUserIdAndStatusAndSentAtBetween(
            Long userId, String status, OffsetDateTime from, OffsetDateTime to);
}
