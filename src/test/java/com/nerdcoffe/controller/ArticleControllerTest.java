package com.nerdcoffe.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import com.nerdcoffe.dto.CreateArticleDto;
import com.nerdcoffe.service.ArticleService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArticleService articleService;

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnBadRequestWithCleanMessageWhenSummaryTooLong() throws Exception {
        String longSummary = "a".repeat(256);

        CreateArticleDto dto = CreateArticleDto.builder()
                .title("Valid Title")
                .content("Valid Content that has more than 10 characters")
                .summary(longSummary)
                .tags(List.of("java"))
                .build();

        mockMvc.perform(post("/api/v1/articles")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("O resumo deve ter no máximo 255 caracteres"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnBadRequestWithCleanMessageWhenTitleIsMissing() throws Exception {
        CreateArticleDto dto = CreateArticleDto.builder()
                .title("")
                .content("Valid Content that has more than 10 characters")
                .summary("Valid Summary")
                .tags(List.of("java"))
                .build();

        mockMvc.perform(post("/api/v1/articles")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void shouldReturnBadRequestWithCleanMessageWhenTagTooLong() throws Exception {
        String longTag = "t".repeat(101);

        CreateArticleDto dto = CreateArticleDto.builder()
                .title("Valid Title")
                .content("Valid Content that has more than 10 characters")
                .summary("Valid Summary")
                .tags(List.of(longTag))
                .build();

        mockMvc.perform(post("/api/v1/articles")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cada tag deve ter no máximo 100 caracteres"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }
}
