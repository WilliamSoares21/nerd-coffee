package com.nerdcoffe.controller;

import com.nerdcoffe.dto.ApiResponseDto;
import com.nerdcoffe.dto.TagDto;
import com.nerdcoffe.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tags")
@Tag(name = "Tags", description = "Endpoints de tags populares")
public class TagController {

    @Autowired
    private ArticleService articleService;

    @GetMapping
    @Operation(summary = "Listar tags populares", description = "Retorna uma lista de tags populares com a respectiva contagem de artigos")
    public ResponseEntity<ApiResponseDto<List<TagDto>>> getPopularTags() {
        log.info("GET /api/v1/tags");
        List<TagDto> tags = articleService.getPopularTags();
        return ResponseEntity.ok(ApiResponseDto.success(tags, "Tags populares recuperadas com sucesso"));
    }
}
