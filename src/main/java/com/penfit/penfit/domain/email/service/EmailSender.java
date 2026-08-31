package com.penfit.penfit.domain.email.service;

public interface EmailSender {

    void send(String to, String subject, String html);
}
