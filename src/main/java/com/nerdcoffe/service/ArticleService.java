package com.nerdcoffe.service;

import com.nerdcoffe.domain.Article;
import com.nerdcoffe.domain.ArticleUpvote;
import com.nerdcoffe.domain.Comment;
import com.nerdcoffe.domain.SavedArticle;
import com.nerdcoffe.domain.User;
import com.nerdcoffe.domain.UserRole;
import com.nerdcoffe.dto.ArticleDto;
import com.nerdcoffe.dto.CommentDto;
import com.nerdcoffe.dto.CreateCommentDto;
import com.nerdcoffe.dto.CreateArticleDto;
import com.nerdcoffe.dto.SavedResponseDto;
import com.nerdcoffe.dto.TagDto;
import com.nerdcoffe.dto.UpvoteResponseDto;
import com.nerdcoffe.dto.UserDto;
import com.nerdcoffe.dto.UserSummaryDto;
import com.nerdcoffe.exception.EntityNotFoundException;
import com.nerdcoffe.repository.ArticleRepository;
import com.nerdcoffe.repository.ArticleUpvoteRepository;
import com.nerdcoffe.repository.CommentRepository;
import com.nerdcoffe.repository.SavedArticleRepository;
import com.nerdcoffe.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

  @Autowired
  private SavedArticleRepository savedArticleRepository;

  @Autowired
  private CommentRepository commentRepository;

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

  @Transactional
  public CommentDto addComment(Long articleId, CreateCommentDto dto) {
    log.info("Adicionando comentário ao artigo: {}", articleId);

    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new EntityNotFoundException("Artigo não encontrado com id: " + articleId));

    User currentUser = getCurrentUser();
    Comment comment = Comment.builder()
        .content(dto.getContent())
        .article(article)
        .author(currentUser)
        .build();

    comment = commentRepository.save(comment);
    log.info("Comentário criado com sucesso: {}", comment.getId());

    return mapToCommentDto(comment);
  }

  @Transactional(readOnly = true)
  public Page<CommentDto> getComments(Long articleId, Pageable pageable) {
    log.info("Buscando comentários do artigo: {}. Page: {}, Size: {}", articleId, pageable.getPageNumber(),
        pageable.getPageSize());

    if (!articleRepository.existsById(articleId)) {
      throw new EntityNotFoundException("Artigo não encontrado com id: " + articleId);
    }

    Pageable sortedPageable = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        Sort.by(Sort.Direction.DESC, "createdAt")
    );

    return commentRepository.findByArticleId(articleId, sortedPageable)
        .map(this::mapToCommentDto);
  }

  @Transactional
  public ArticleDto archiveArticle(Long id) {
    log.info("Arquivando artigo: {}", id);

    Article article = articleRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Artigo não encontrado com id: " + id));

    User currentUser = getCurrentUser();
    if (!article.getAuthor().getId().equals(currentUser.getId()) && !isAdmin()) {
      log.warn("Usuário não autorizado a arquivar artigo: {}", id);
      throw new AccessDeniedException("Você não tem permissão para arquivar este artigo");
    }

    article.setPublished(false);
    article = articleRepository.save(article);
    log.info("Artigo arquivado com sucesso: {}", article.getId());

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
  public Page<ArticleDto> getAllPublishedArticles(String tag, String author, Pageable pageable) {
    log.info("Buscando todos os artigos publicados com tag: {} e autor: {}. Page: {}, Size: {}", tag, author, pageable.getPageNumber(),
        pageable.getPageSize());
    Long currentUserId = getCurrentUserIdOrNull();
    return articleRepository.findAllPublished(tag, author, pageable)
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

  @Transactional
  public SavedResponseDto toggleSaveArticle(Long articleId) {
    log.info("Alternando salvamento para artigo: {}", articleId);

    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new EntityNotFoundException("Artigo não encontrado com id: " + articleId));

    User currentUser = getCurrentUser();

    Optional<SavedArticle> existingSaved = savedArticleRepository.findByArticleAndUser(articleId, currentUser.getId());

    boolean saved;
    String message;

    if (existingSaved.isPresent()) {
      savedArticleRepository.delete(existingSaved.get());
      saved = false;
      message = "Artigo removido dos salvos";
      log.info("Artigo {} removido dos salvos pelo usuário {}", articleId, currentUser.getId());
    } else {
      SavedArticle newSaved = SavedArticle.builder()
          .article(article)
          .user(currentUser)
          .build();
      savedArticleRepository.save(newSaved);
      saved = true;
      message = "Artigo salvo com sucesso";
      log.info("Artigo {} salvo pelo usuário {}", articleId, currentUser.getId());
    }

    return SavedResponseDto.builder()
        .saved(saved)
        .message(message)
        .build();
  }

  @Transactional(readOnly = true)
  public Page<ArticleDto> getSavedArticles(Pageable pageable) {
    log.info("Buscando artigos salvos. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
    User currentUser = getCurrentUser();
    return savedArticleRepository.findSavedArticlesByUserId(currentUser.getId(), pageable)
        .map(article -> mapToDto(article, currentUser.getId()));
  }

  @Transactional(readOnly = true)
  public Page<ArticleDto> searchGlobally(String query, Pageable pageable) {
    log.info("Buscando artigos globalmente com termo: {}", query);
    Long currentUserId = getCurrentUserIdOrNull();
    return articleRepository.searchGlobally(query, pageable)
        .map(article -> mapToDto(article, currentUserId));
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String identifier = authentication.getName();
    return userRepository.findByEmail(identifier)
        .or(() -> userRepository.findByUsername(identifier))
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
    Long commentsCount = commentRepository.countByArticleId(article.getId());
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAuthenticated = authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
    boolean isUpvoted = false;
    boolean isSaved = false;

    if (isAuthenticated) {
      Long userId = currentUserId;
      if (userId == null) {
        User currentUser = getCurrentUser();
        userId = currentUser.getId();
      }
      isUpvoted = articleUpvoteRepository.existsByArticleIdAndUserId(article.getId(), userId);
      isSaved = savedArticleRepository.existsByArticleIdAndUserId(article.getId(), userId);
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
            .bio(article.getAuthor().getBio())
            .avatarUrl(article.getAuthor().getAvatarUrl())
            .username(article.getAuthor().getUsername())
            .build())
        .published(article.getPublished())
        .tags(article.getTags() == null ? List.of() : List.copyOf(article.getTags()))
        .createdAt(article.getCreatedAt())
        .updatedAt(article.getUpdatedAt())
        .publishedAt(article.getPublishedAt())
        .upvoteCount(upvoteCount)
        .userUpvoted(isUpvoted)
        .isSaved(isSaved)
        .commentsCount(commentsCount)
        .build();
  }

  private CommentDto mapToCommentDto(Comment comment) {
    User author = comment.getAuthor();
    return CommentDto.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .author(UserSummaryDto.builder()
            .id(author.getId())
            .name(author.getName())
            .username(author.getUsername())
            .avatarUrl(author.getAvatarUrl())
            .build())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .build();
  }

  private List<String> resolveTags(List<String> incomingTags, List<String> currentTags) {
    if (incomingTags == null) {
        return currentTags == null ? new ArrayList<>() : new ArrayList<>(currentTags);
    }
    return new ArrayList<>(incomingTags);
  }
}
