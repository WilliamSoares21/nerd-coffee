package com.nerdcoffe.security;

import com.nerdcoffe.dto.ApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String message = "Não autenticado";

        if ("POST".equalsIgnoreCase(method)) {
            if (uri.equals("/api/v1/articles") || uri.equals("/api/v1/articles/")
                    || uri.matches("/api/v1/articles/[^/]+/upvote/?")
                    || uri.matches("/api/v1/articles/[^/]+/save/?")
                    || uri.matches("/api/v1/articles/[^/]+/comments/?")) {
                message = "Você precisa estar logado para realizar esta ação";
            }
        }

        ApiResponseDto<Void> body = ApiResponseDto.error(message);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        OBJECT_MAPPER.writeValue(response.getOutputStream(), body);
    }
}
