package com.penfit.penfit.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.penfit.penfit.global.common.ApiResTemplate;
import com.penfit.penfit.global.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode errorCode = resolveErrorCode(request);
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResTemplate.error(errorCode));
    }

    private ErrorCode resolveErrorCode(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthErrorAttribute.NAME);
        if (attribute instanceof ErrorCode errorCode) {
            return errorCode;
        }
        return ErrorCode.UNAUTHORIZED;
    }
}
