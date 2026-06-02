package com.nerdcoffe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreateCommentDto {

    @NotBlank(message = "Conteúdo do comentário é obrigatório")
    private String content;
}
