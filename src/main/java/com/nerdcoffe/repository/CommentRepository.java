package com.nerdcoffe.repository;

import com.nerdcoffe.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByArticleId(Long articleId, Pageable pageable);
    Page<Comment> findByArticleIdAndParentCommentIsNull(Long articleId, Pageable pageable);
    long countByArticleId(Long articleId);
}
