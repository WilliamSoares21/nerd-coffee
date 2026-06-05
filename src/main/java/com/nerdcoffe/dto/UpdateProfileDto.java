package com.nerdcoffe.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UpdateProfileDto {
    private String name;

    @Size(max = 1000, message = "A biografia deve ter no máximo 1000 caracteres")
    private String bio;

    @Size(max = 255, message = "A URL do avatar deve ter no máximo 255 caracteres")
    private String avatarUrl;

    @Size(max = 50, message = "Username deve ter no máximo 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username inválido")
    private String username;
}
