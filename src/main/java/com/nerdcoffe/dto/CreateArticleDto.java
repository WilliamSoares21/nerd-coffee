package com.nerdcoffe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreateArticleDto {

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 5, max = 200, message = "Título deve ter entre 5 e 200 caracteres")
    private String title;

    @NotBlank(message = "Conteúdo é obrigatório")
    @Size(min = 10, message = "Conteúdo deve ter pelo menos 10 caracteres")
    private String content;

    @Size(max = 500, message = "Resumo não pode exceder 500 caracteres")
    private String summary;
}
