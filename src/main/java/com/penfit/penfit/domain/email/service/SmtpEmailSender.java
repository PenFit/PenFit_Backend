package com.penfit.penfit.domain.email.service;

import com.penfit.penfit.global.config.MailProperties;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    @Override
    public void send(String to, String subject, String html) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(
                    mailProperties.from(), mailProperties.fromName(), StandardCharsets.UTF_8.name()));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
        } catch (jakarta.mail.MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("메일 메시지를 만들지 못했습니다.", e);
        }
        javaMailSender.send(message);
    }
}
