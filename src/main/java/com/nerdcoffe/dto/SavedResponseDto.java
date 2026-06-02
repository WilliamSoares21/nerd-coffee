package com.nerdcoffe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO para resposta de toggle de salvar artigo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedResponseDto {

    @JsonProperty("saved")
    private boolean saved;

    @JsonProperty("message")
    private String message;
}
