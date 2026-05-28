package com.nerdcoffe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO para resposta de toggle de upvote.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpvoteResponseDto {

    @JsonProperty("upvoted")
    private boolean upvoted;

    @JsonProperty("upvoteCount")
    private Long upvoteCount;

    @JsonProperty("message")
    private String message;
}
