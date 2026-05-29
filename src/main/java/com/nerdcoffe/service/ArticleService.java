package com.nerdcoffe.service;

import com.nerdcoffe.domain.Article;
import com.nerdcoffe.domain.ArticleUpvote;
import com.nerdcoffe.domain.User;
import com.nerdcoffe.domain.UserRole;
import com.nerdcoffe.dto.ArticleDto;
import com.nerdcoffe.dto.CreateArticleDto;
import com.nerdcoffe.dto.TagDto;
import com.nerdcoffe.dto.UpvoteResponseDto;
import com.nerdcoffe.dto.UserDto;
import com.nerdcoffe.exception.EntityNotFoundException;
import com.nerdcoffe.repository.ArticleRepository;
import com.nerdcoffe.repository.ArticleUpvoteRepository;
import com.nerdcoffe.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ArticleService {

  @Autowired
  private ArticleRepository articleRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ArticleUpvoteRepository articleUpvoteRepository;

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
        .tags(resolveTags(dto.getTags(), null))
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
      throw new AccessDeniedException("Você não tem permissão para atualizar este artigo");
    }

    article.setTitle(dto.getTitle());
    article.setContent(dto.getContent());
    article.setSummary(dto.getSummary());
    article.setTags(resolveTags(dto.getTags(), article.getTags()));

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
      throw new AccessDeniedException("Você não tem permissão para deletar este artigo");
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
      throw new AccessDeniedException("Você não tem permissão para publicar este artigo");
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
    
    Long currentUserId = getCurrentUserIdOrNull();
    return mapToDto(article, currentUserId);
  }

  @Transactional(readOnly = true)
  public Page<ArticleDto> getAllPublishedArticles(String tag, Pageable pageable) {
    log.info("Buscando todos os artigos publicados com tag: {}. Page: {}, Size: {}", tag, pageable.getPageNumber(),
        pageable.getPageSize());
    Long currentUserId = getCurrentUserIdOrNull();
    return articleRepository.findAllPublished(tag, pageable)
        .map(article -> mapToDto(article, currentUserId));
  }

  @Transactional(readOnly = true)
  public Page<ArticleDto> getTrendingArticles(Pageable pageable) {
    log.info("Buscando artigos trending. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
    Long currentUserId = getCurrentUserIdOrNull();
    return articleRepository.findTrending(pageable)
        .map(article -> mapToDto(article, currentUserId));
  }

  @Transactional(readOnly = true)
  public List<TagDto> getPopularTags() {
    log.info("Buscando tags populares");
    return articleRepository.findPopularTags();
  }

  @Transactional(readOnly = true)
  public Page<ArticleDto> getMyArticles(Pageable pageable) {
    log.info("Buscando meus artigos. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
    User currentUser = getCurrentUser();
    return articleRepository.findByAuthor(currentUser, pageable)
        .map(article -> mapToDto(article, currentUser.getId()));
  }

  @Transactional(readOnly = true)
  public Page<ArticleDto> searchPublishedArticles(String title, Pageable pageable) {
    log.info("Buscando artigos com título: {}", title);
    Long currentUserId = getCurrentUserIdOrNull();
    return articleRepository.searchPublishedByTitle(title, pageable)
        .map(article -> mapToDto(article, currentUserId));
  }

  @Transactional
  public UpvoteResponseDto toggleUpvote(Long articleId) {
    log.info("Alternando upvote para artigo: {}", articleId);

    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new EntityNotFoundException("Artigo não encontrado com id: " + articleId));

    User currentUser = getCurrentUser();

    // Verifica se o usuário já deu upvote
    Optional<ArticleUpvote> existingUpvote = articleUpvoteRepository.findByArticleAndUser(articleId, currentUser.getId());

    boolean upvoted;
    String message;

    if (existingUpvote.isPresent()) {
      // Remove o upvote (toggle off)
      articleUpvoteRepository.delete(existingUpvote.get());
      upvoted = false;
      message = "Upvote removido com sucesso";
      log.info("Upvote removido do artigo {} por usuário {}", articleId, currentUser.getId());
    } else {
      // Adiciona novo upvote (toggle on)
      ArticleUpvote newUpvote = ArticleUpvote.builder()
          .article(article)
          .user(currentUser)
          .build();
      articleUpvoteRepository.save(newUpvote);
      upvoted = true;
      message = "Upvote adicionado com sucesso";
      log.info("Upvote adicionado ao artigo {} por usuário {}", articleId, currentUser.getId());
    }

    Long upvoteCount = articleUpvoteRepository.countByArticleId(articleId);

    return UpvoteResponseDto.builder()
        .upvoted(upvoted)
        .upvoteCount(upvoteCount)
        .message(message)
        .build();
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("Usuário atual não encontrado"));
  }

  private Long getCurrentUserIdOrNull() {
    try {
      return getCurrentUser().getId();
    } catch (Exception e) {
      log.debug("Nenhum usuário autenticado no contexto");
      return null;
    }
  }

  private boolean isAdmin() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getAuthorities().stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
  }

  private ArticleDto mapToDto(Article article) {
    return mapToDto(article, getCurrentUserIdOrNull());
  }

  private ArticleDto mapToDto(Article article, Long currentUserId) {
    Long upvoteCount = articleUpvoteRepository.countByArticleId(article.getId());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAuthenticated = authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
    boolean isUpvoted = false;

    if (isAuthenticated) {
      Long userId = currentUserId;
      if (userId == null) {
        User currentUser = getCurrentUser();
        userId = currentUser.getId();
      }
      isUpvoted = articleUpvoteRepository.existsByArticleIdAndUserId(article.getId(), userId);
    }

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
        .tags(article.getTags() == null ? List.of() : List.copyOf(article.getTags()))
        .createdAt(article.getCreatedAt())
        .updatedAt(article.getUpdatedAt())
        .publishedAt(article.getPublishedAt())
        .upvoteCount(upvoteCount)
        .userUpvoted(isUpvoted)
        .build();
  }

  private List<String> resolveTags(List<String> incomingTags, List<String> currentTags) {
    if (incomingTags == null) {
        return currentTags == null ? new ArrayList<>() : new ArrayList<>(currentTags);
    }
    return new ArrayList<>(incomingTags);
  }
}
