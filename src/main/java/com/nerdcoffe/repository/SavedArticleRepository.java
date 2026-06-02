package com.nerdcoffe.repository;

import com.nerdcoffe.domain.Article;
import com.nerdcoffe.domain.SavedArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedArticleRepository extends JpaRepository<SavedArticle, Long> {

    @Query("SELECT s FROM SavedArticle s WHERE s.article.id = :articleId AND s.user.id = :userId")
    Optional<SavedArticle> findByArticleAndUser(@Param("articleId") Long articleId, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SavedArticle s WHERE s.article.id = :articleId AND s.user.id = :userId")
    boolean existsByArticleIdAndUserId(@Param("articleId") Long articleId, @Param("userId") Long userId);

    @Query("SELECT s.article FROM SavedArticle s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    Page<Article> findSavedArticlesByUserId(@Param("userId") Long userId, Pageable pageable);
}
