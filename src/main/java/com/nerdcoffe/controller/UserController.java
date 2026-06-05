package com.nerdcoffe.controller;

import com.nerdcoffe.dto.ApiResponseDto;
import com.nerdcoffe.dto.UpdateProfileDto;
import com.nerdcoffe.dto.UserProfileDto;
import com.nerdcoffe.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuários", description = "Endpoints de gerenciamento de usuários")
public class UserController {

  @Autowired
  private UserService userService;

  @GetMapping("/{username}")
  @Operation(summary = "Obter perfil público de usuário", description = "Retorna os dados públicos do perfil do usuário com base no username")
  public ResponseEntity<ApiResponseDto<UserProfileDto>> getUserProfile(@PathVariable String username) {
    log.info("GET /api/v1/users/{}", username);
    UserProfileDto userProfile = userService.getUserProfile(username);
    return ResponseEntity.ok(ApiResponseDto.success(userProfile, "Perfil do usuário recuperado com sucesso"));
  }

  @PutMapping("/profile")
  @SecurityRequirement(name = "bearer-jwt")
  @Operation(summary = "Atualizar perfil do usuário logado", description = "Atualiza username, name, bio e avatarUrl do usuário autenticado no sistema")
  public ResponseEntity<ApiResponseDto<UserProfileDto>> updateProfile(@Valid @RequestBody UpdateProfileDto dto) {
    log.info("PUT /api/v1/users/profile");
    UserProfileDto userProfile = userService.updateProfile(dto);
    return ResponseEntity.ok(ApiResponseDto.success(userProfile, "Perfil atualizado com sucesso"));
  }
}
