package com.nerdcoffe.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidade que representa um upvote de um usuário em um artigo.
 * Utiliza uma tabela de relacionamento Many-to-Many para garantir
 * que um usuário possa dar apenas um upvote por artigo.
 */
@Entity
@Table(
        name = "article_upvotes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"article_id", "user_id"})
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ArticleUpvote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleUpvote that = (ArticleUpvote) o;
        return article != null && article.equals(that.article) &&
               user != null && user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return 31 * (article != null ? article.hashCode() : 0) +
               (user != null ? user.hashCode() : 0);
    }
}
