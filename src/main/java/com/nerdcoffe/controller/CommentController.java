package com.nerdcoffe.controller;

import com.nerdcoffe.dto.ApiResponseDto;
import com.nerdcoffe.dto.UpvoteResponseDto;
import com.nerdcoffe.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "Comentários", description = "Endpoints de gerenciamento de interações com comentários")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/{id}/upvote")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Dar/remover upvote em comentário", description = "Alterna o upvote de um comentário para o usuário autenticado")
    public ResponseEntity<ApiResponseDto<UpvoteResponseDto>> toggleUpvote(@PathVariable Long id) {
        log.info("POST /api/v1/comments/{}/upvote", id);
        UpvoteResponseDto response = commentService.toggleUpvote(id);
        return ResponseEntity.ok(ApiResponseDto.success(response, response.getMessage()));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Deletar comentário", description = "Apaga um comentário (apenas autor do comentário ou admin)")
    public ResponseEntity<ApiResponseDto<Void>> deleteComment(@PathVariable Long id) {
        log.info("DELETE /api/v1/comments/{}", id);
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponseDto.success(null, "Comentário deletado com sucesso"));
    }
}
