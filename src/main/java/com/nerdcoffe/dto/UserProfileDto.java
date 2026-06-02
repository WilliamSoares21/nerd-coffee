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
public class UserProfileDto {
    private Long id;
    private String name;
    private String username;
    private String avatarUrl;
    private String bio;
    private LocalDateTime createdAt;
}
