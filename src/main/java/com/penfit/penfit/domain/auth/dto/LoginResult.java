package com.penfit.penfit.domain.auth.dto;

public record LoginResult(LoginResponse response, TokenBundle tokens) {
}
