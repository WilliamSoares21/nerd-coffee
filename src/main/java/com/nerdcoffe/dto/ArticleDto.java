package com.nerdcoffe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ArticleDto {
  private Long id;
  private String title;
  private String content;
  private String summary;
  private UserDto author;
  private Boolean published;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime publishedAt;
  private List<String> tags;

  @JsonProperty("upvoteCount")
  private Long upvoteCount;

  @JsonProperty("userUpvoted")
  private Boolean userUpvoted;

  @JsonProperty("isSaved")
  private Boolean isSaved;
}
