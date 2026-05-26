package com.nerdcoffe.service;

import com.nerdcoffe.domain.Article;
import com.nerdcoffe.domain.User;
import com.nerdcoffe.dto.ArticleDto;
import com.nerdcoffe.dto.CreateArticleDto;
import com.nerdcoffe.dto.UserDto;
import com.nerdcoffe.exception.EntityNotFoundException;
import com.nerdcoffe.repository.ArticleRepository;
import com.nerdcoffe.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ArticleDto createArticle(CreateArticleDto dto) {
        log.info("Criando novo artigo: {}", dto.getTitle());

        User author = getCurrentUser();
        
        Article article = Article.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .summary(dto.getSummary())
                .author(author)
                .published(false)
                .build();

        article = articleRepository.save(article);
        log.info("Artigo criado com sucesso: {}", article.getId());

        return mapToDto(article);
    }

    @Transactional
    public ArticleDto updateArticle(Long id, CreateArticleDto dto) {
        log.info("Atualizando artigo: {}", id);

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artigo não encontrado com id: " + id));

        User currentUser = getCurrentUser();
        if (!article.getAuthor().getId().equals(currentUser.getId()) && !isAdmin()) {
            log.warn("Usuário não autorizado a atualizar artigo: {}", id);
            throw new EntityNotFoundException("Você não tem permissão para atualizar este artigo");
        }

        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setSummary(dto.getSummary());

        article = articleRepository.save(article);
        log.info("Artigo atualizado com sucesso: {}", article.getId());

        return mapToDto(article);
    }

    @Transactional
    public void deleteArticle(Long id) {
        log.info("Deletando artigo: {}", id);

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artigo não encontrado com id: " + id));

        User currentUser = getCurrentUser();
        if (!article.getAuthor().getId().equals(currentUser.getId()) && !isAdmin()) {
            log.warn("Usuário não autorizado a deletar artigo: {}", id);
            throw new EntityNotFoundException("Você não tem permissão para deletar este artigo");
        }

        articleRepository.deleteById(id);
        log.info("Artigo deletado com sucesso: {}", id);
    }

    @Transactional
    public ArticleDto publishArticle(Long id) {
        log.info("Publicando artigo: {}", id);

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artigo não encontrado com id: " + id));

        User currentUser = getCurrentUser();
        if (!article.getAuthor().getId().equals(currentUser.getId()) && !isAdmin()) {
            log.warn("Usuário não autorizado a publicar artigo: {}", id);
            throw new EntityNotFoundException("Você não tem permissão para publicar este artigo");
        }

        article.setPublished(true);
        article.setPublishedAt(LocalDateTime.now());
        article = articleRepository.save(article);
        log.info("Artigo publicado com sucesso: {}", article.getId());

        return mapToDto(article);
    }

    @Transactional(readOnly = true)
    public ArticleDto getArticleById(Long id) {
        log.info("Buscando artigo por id: {}", id);
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Artigo não encontrado com id: " + id));
        return mapToDto(article);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDto> getAllPublishedArticles(int page, int size) {
        log.info("Buscando todos os artigos publicados. Page: {}, Size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findAllPublished(pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDto> getMyArticles(int page, int size) {
        log.info("Buscando meus artigos. Page: {}, Size: {}", page, size);
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findByAuthor(currentUser, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDto> searchPublishedArticles(String title, int page, int size) {
        log.info("Buscando artigos com título: {}", title);
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.searchPublishedByTitle(title, pageable)
                .map(this::mapToDto);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário atual não encontrado"));
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    private ArticleDto mapToDto(Article article) {
        return ArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .author(UserDto.builder()
                        .id(article.getAuthor().getId())
                        .name(article.getAuthor().getName())
                        .email(article.getAuthor().getEmail())
                        .role(article.getAuthor().getRole())
                        .build())
                .published(article.getPublished())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .publishedAt(article.getPublishedAt())
                .build();
    }
}
