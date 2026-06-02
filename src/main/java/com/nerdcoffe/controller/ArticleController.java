package com.nerdcoffe.controller;

import com.nerdcoffe.dto.ApiResponseDto;
import com.nerdcoffe.dto.ArticleDto;
import com.nerdcoffe.dto.CommentDto;
import com.nerdcoffe.dto.CreateCommentDto;
import com.nerdcoffe.dto.CreateArticleDto;
import com.nerdcoffe.dto.PageResponseDto;
import com.nerdcoffe.dto.SavedResponseDto;
import com.nerdcoffe.dto.TagDto;
import com.nerdcoffe.dto.UpvoteResponseDto;
import com.nerdcoffe.service.ArticleService;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @PostMapping("/{id}/comments")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Adicionar comentário", description = "Adiciona um comentário ao artigo (usuário autenticado)")
    public ResponseEntity<ApiResponseDto<CommentDto>> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentDto dto) {
        log.info("POST /api/v1/articles/{}/comments", id);
        CommentDto commentDto = articleService.addComment(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(commentDto, "Comentário criado com sucesso"));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Listar comentários do artigo", description = "Retorna uma lista paginada de comentários do artigo")
    public ResponseEntity<ApiResponseDto<PageResponseDto<CommentDto>>> getComments(
            @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("GET /api/v1/articles/{}/comments?page={}&size={}", id, pageable.getPageNumber(),
                pageable.getPageSize());
        Page<CommentDto> comments = articleService.getComments(id, pageable);
        PageResponseDto<CommentDto> response = PageResponseDto.fromPage(comments);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Comentários recuperados com sucesso"));
    }

    @PostMapping("/{id}/upvote")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Dar/remover upvote em artigo",
            description = "Funciona como um toggle: adiciona upvote se o usuário ainda não deu, ou remove se já tiver dado. " +
                    "Disponível para todos os usuários autenticados (VIEWER, EDITOR, ADMIN)"
    )
    public ResponseEntity<ApiResponseDto<UpvoteResponseDto>> toggleUpvote(@PathVariable Long id) {
        log.info("POST /api/v1/articles/{}/upvote", id);
        UpvoteResponseDto response = articleService.toggleUpvote(id);
        return ResponseEntity.ok(ApiResponseDto.success(response, response.getMessage()));
    }

    @PostMapping("/{id}/save")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Salvar/remover artigo dos favoritos",
            description = "Funciona como um toggle: adiciona o artigo aos salvos se ainda não estiver, ou remove se já estiver salvo. " +
                    "Disponível para todos os usuários autenticados (VIEWER, EDITOR, ADMIN)"
    )
    public ResponseEntity<ApiResponseDto<SavedResponseDto>> toggleSaveArticle(@PathVariable Long id) {
        log.info("POST /api/v1/articles/{}/save", id);
        SavedResponseDto response = articleService.toggleSaveArticle(id);
        return ResponseEntity.ok(ApiResponseDto.success(response, response.getMessage()));
    }

    @GetMapping("/saved")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Listar artigos salvos", description = "Retorna uma lista paginada dos artigos salvos pelo usuário autenticado")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ArticleDto>>> getSavedArticles(
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("GET /api/v1/articles/saved?page={}&size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ArticleDto> articles = articleService.getSavedArticles(pageable);
        PageResponseDto<ArticleDto> response = PageResponseDto.fromPage(articles);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Artigos salvos recuperados com sucesso"));
    }

    @GetMapping("/search")
    @Operation(summary = "Pesquisar artigos globalmente", description = "Pesquisa artigos publicados por título ou resumo")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ArticleDto>>> searchGlobally(
            @RequestParam("q") String query,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("GET /api/v1/articles/search?q={}&page={}&size={}", query, pageable.getPageNumber(), pageable.getPageSize());
        Page<ArticleDto> articles = articleService.searchGlobally(query, pageable);
        PageResponseDto<ArticleDto> response = PageResponseDto.fromPage(articles);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Busca global realizada com sucesso"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter artigo por ID", description = "Retorna detalhes de um artigo específico")
    public ResponseEntity<ApiResponseDto<ArticleDto>> getArticleById(@PathVariable Long id) {
        log.info("GET /api/v1/articles/{}", id);
        ArticleDto articleDto = articleService.getArticleById(id);
        return ResponseEntity.ok(ApiResponseDto.success(articleDto));
    }

    @GetMapping("/public")
    @Operation(summary = "Listar artigos publicados (rota pública)", description = "Retorna uma lista paginada de artigos publicados com filtro opcional de tag")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ArticleDto>>> getPublishedArticles(
            @RequestParam(required = false) String tag,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("GET /api/v1/articles/public?page={}&size={}&tag={}", pageable.getPageNumber(), pageable.getPageSize(), tag);
        Page<ArticleDto> articles = articleService.getAllPublishedArticles(tag, pageable);
        PageResponseDto<ArticleDto> response = PageResponseDto.fromPage(articles);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Artigos recuperados com sucesso"));
    }

    @GetMapping("/public/all")
    @Operation(summary = "Listar todos os artigos publicados", description = "Retorna uma lista paginada de todos os artigos publicados")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ArticleDto>>> getAllPublishedArticles(
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("GET /api/v1/articles/public/all?page={}&size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ArticleDto> articles = articleService.getAllPublishedArticles(null, pageable);
        PageResponseDto<ArticleDto> response = PageResponseDto.fromPage(articles);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Artigos recuperados com sucesso"));
    }

    @GetMapping("/public/trending")
    @Operation(summary = "Listar artigos populares (trending)", description = "Retorna uma lista paginada de artigos publicados ordenados por engajamento/relevância")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ArticleDto>>> getTrendingArticles(
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("GET /api/v1/articles/public/trending?page={}&size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ArticleDto> articles = articleService.getTrendingArticles(pageable);
        PageResponseDto<ArticleDto> response = PageResponseDto.fromPage(articles);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Artigos em alta recuperados com sucesso"));
    }

    @GetMapping("/public/search")
    @Operation(summary = "Pesquisar artigos publicados", description = "Pesquisa artigos publicados por título")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ArticleDto>>> searchPublishedArticles(
            @RequestParam String title,
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("GET /api/v1/articles/public/search?title={}&page={}&size={}", title, pageable.getPageNumber(), pageable.getPageSize());
        Page<ArticleDto> articles = articleService.searchPublishedArticles(title, pageable);
        PageResponseDto<ArticleDto> response = PageResponseDto.fromPage(articles);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Busca realizada com sucesso"));
    }

    @GetMapping("/my-articles")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Meus artigos", description = "Retorna todos os artigos criados pelo usuário autenticado")
    public ResponseEntity<ApiResponseDto<PageResponseDto<ArticleDto>>> getMyArticles(
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("GET /api/v1/articles/my-articles?page={}&size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ArticleDto> articles = articleService.getMyArticles(pageable);
        PageResponseDto<ArticleDto> response = PageResponseDto.fromPage(articles);
        return ResponseEntity.ok(ApiResponseDto.success(response, "Artigos recuperados com sucesso"));
    }
}
