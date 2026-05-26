package com.nerdcoffe.controller;

import com.nerdcoffe.dto.ApiResponseDto;
import com.nerdcoffe.dto.ArticleDto;
import com.nerdcoffe.dto.CreateArticleDto;
import com.nerdcoffe.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/articles")
@Tag(name = "Artigos", description = "Endpoints de gerenciamento de artigos")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @PostMapping
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Criar novo artigo", description = "Cria um novo artigo (requer autenticação com role EDITOR ou ADMIN)")
    public ResponseEntity<ApiResponseDto<ArticleDto>> createArticle(@Valid @RequestBody CreateArticleDto dto) {
        log.info("POST /api/v1/articles");
        ArticleDto articleDto = articleService.createArticle(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(articleDto, "Artigo criado com sucesso"));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Atualizar artigo", description = "Atualiza um artigo existente (apenas autor ou admin)")
    public ResponseEntity<ApiResponseDto<ArticleDto>> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody CreateArticleDto dto) {
        log.info("PUT /api/v1/articles/{}", id);
        ArticleDto articleDto = articleService.updateArticle(id, dto);
        return ResponseEntity.ok(ApiResponseDto.success(articleDto, "Artigo atualizado com sucesso"));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Deletar artigo", description = "Deleta um artigo (apenas admin)")
    public ResponseEntity<ApiResponseDto<Void>> deleteArticle(@PathVariable Long id) {
        log.info("DELETE /api/v1/articles/{}", id);
        articleService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponseDto.success(null, "Artigo deletado com sucesso"));
    }

    @PatchMapping("/{id}/publish")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Publicar artigo", description = "Publica um artigo (apenas autor ou admin)")
    public ResponseEntity<ApiResponseDto<ArticleDto>> publishArticle(@PathVariable Long id) {
        log.info("PATCH /api/v1/articles/{}/publish", id);
        ArticleDto articleDto = articleService.publishArticle(id);
        return ResponseEntity.ok(ApiResponseDto.success(articleDto, "Artigo publicado com sucesso"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter artigo por ID", description = "Retorna detalhes de um artigo específico")
    public ResponseEntity<ApiResponseDto<ArticleDto>> getArticleById(@PathVariable Long id) {
        log.info("GET /api/v1/articles/{}", id);
        ArticleDto articleDto = articleService.getArticleById(id);
        return ResponseEntity.ok(ApiResponseDto.success(articleDto));
    }

    @GetMapping("/public/all")
    @Operation(summary = "Listar todos os artigos publicados", description = "Retorna uma lista paginada de todos os artigos publicados")
    public ResponseEntity<ApiResponseDto<Page<ArticleDto>>> getAllPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/articles/public/all?page={}&size={}", page, size);
        Page<ArticleDto> articles = articleService.getAllPublishedArticles(page, size);
        return ResponseEntity.ok(ApiResponseDto.success(articles, "Artigos recuperados com sucesso"));
    }

    @GetMapping("/public/search")
    @Operation(summary = "Pesquisar artigos publicados", description = "Pesquisa artigos publicados por título")
    public ResponseEntity<ApiResponseDto<Page<ArticleDto>>> searchPublishedArticles(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/articles/public/search?title={}&page={}&size={}", title, page, size);
        Page<ArticleDto> articles = articleService.searchPublishedArticles(title, page, size);
        return ResponseEntity.ok(ApiResponseDto.success(articles, "Busca realizada com sucesso"));
    }

    @GetMapping("/my-articles")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Meus artigos", description = "Retorna todos os artigos criados pelo usuário autenticado")
    public ResponseEntity<ApiResponseDto<Page<ArticleDto>>> getMyArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/v1/articles/my-articles?page={}&size={}", page, size);
        Page<ArticleDto> articles = articleService.getMyArticles(page, size);
        return ResponseEntity.ok(ApiResponseDto.success(articles, "Artigos recuperados com sucesso"));
    }
}
