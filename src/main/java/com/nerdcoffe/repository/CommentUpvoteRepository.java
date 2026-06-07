package com.nerdcoffe.repository;

import com.nerdcoffe.domain.CommentUpvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentUpvoteRepository extends JpaRepository<CommentUpvote, Long> {

    @Query("SELECT c FROM CommentUpvote c WHERE c.comment.id = :commentId AND c.user.id = :userId")
    Optional<CommentUpvote> findByCommentAndUser(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM CommentUpvote c WHERE c.comment.id = :commentId")
    Long countByCommentId(@Param("commentId") Long commentId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CommentUpvote c WHERE c.comment.id = :commentId AND c.user.id = :userId")
    boolean existsByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM CommentUpvote c WHERE c.comment.id = :commentId")
    void deleteByCommentId(@Param("commentId") Long commentId);
}
