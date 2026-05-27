package com.nerdcoffe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class LoginResponseDto {
    private String token;
    private String type = "Bearer";
    private Long expiresIn;
    private Long userId;
    private List<String> roles;
    private UserDto user;
}
