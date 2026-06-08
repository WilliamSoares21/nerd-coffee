package com.nerdcoffe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CreateArticleDto {

    @NotBlank(message = "Título é obrigatório")
    @Size(min = 5, max = 120, message = "O título deve ter entre 5 e 120 caracteres")
    private String title;

    @NotBlank(message = "Conteúdo é obrigatório")
    @Size(min = 10, message = "Conteúdo deve ter pelo menos 10 caracteres")
    private String content;

    @Size(max = 255, message = "O resumo deve ter no máximo 255 caracteres")
    private String summary;

    private List<@Size(max = 100, message = "Cada tag deve ter no máximo 100 caracteres") String> tags;
}
