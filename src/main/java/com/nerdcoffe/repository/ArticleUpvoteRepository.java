package com.nerdcoffe.repository;

import com.nerdcoffe.domain.ArticleUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleUpvoteRepository extends JpaRepository<ArticleUpvote, Long> {

    /**
     * Busca um upvote específico por artigo e usuário.
     *
     * @param articleId ID do artigo
     * @param userId    ID do usuário
     * @return Optional contendo o upvote se existir
     */
    @Query("SELECT a FROM ArticleUpvote a WHERE a.article.id = :articleId AND a.user.id = :userId")
    Optional<ArticleUpvote> findByArticleAndUser(@Param("articleId") Long articleId, @Param("userId") Long userId);

    /**
     * Conta quantos upvotes um artigo tem.
     *
     * @param articleId ID do artigo
     * @return Quantidade de upvotes
     */
    @Query("SELECT COUNT(a) FROM ArticleUpvote a WHERE a.article.id = :articleId")
    Long countByArticleId(@Param("articleId") Long articleId);

    /**
     * Verifica se um usuário deu upvote em um artigo específico.
     *
     * @param articleId ID do artigo
     * @param userId    ID do usuário
     * @return true se o usuário deu upvote, false caso contrário
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM ArticleUpvote a WHERE a.article.id = :articleId AND a.user.id = :userId")
    boolean existsByArticleAndUser(@Param("articleId") Long articleId, @Param("userId") Long userId);
}
