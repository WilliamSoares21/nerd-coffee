package com.nerdcoffe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CommentDto {
    private Long id;
    private String content;
    private UserSummaryDto author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
