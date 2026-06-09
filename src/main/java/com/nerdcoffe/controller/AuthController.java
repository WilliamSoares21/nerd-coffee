package com.nerdcoffe.controller;

import com.nerdcoffe.dto.ApiResponseDto;
import com.nerdcoffe.dto.CreateUserDto;
import com.nerdcoffe.dto.LoginRequestDto;
import com.nerdcoffe.dto.LoginResponseDto;
import com.nerdcoffe.dto.UserDto;
import com.nerdcoffe.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação e registro")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário")
    public ResponseEntity<ApiResponseDto<UserDto>> register(@Valid @RequestBody CreateUserDto dto) {
        log.info("POST /api/v1/auth/register");
        UserDto userDto = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(userDto, "Usuário registrado com sucesso"));
    }

    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Autentica o usuário e retorna um token JWT")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto dto) {
        log.info("POST /api/v1/auth/login");
        LoginResponseDto response = authService.login(dto);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Login realizado com sucesso"));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verificar e-mail", description = "Verifica o e-mail do usuário utilizando o token enviado")
    public ResponseEntity<ApiResponseDto<Void>> verifyEmail(@RequestParam("token") String token) {
        log.info("GET /api/v1/auth/verify-email");
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponseDto.success(null, "E-mail verificado com sucesso"));
    }
}
