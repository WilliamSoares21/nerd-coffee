package com.nerdcoffe.service;

import com.nerdcoffe.domain.Comment;
import com.nerdcoffe.domain.CommentUpvote;
import com.nerdcoffe.domain.User;
import com.nerdcoffe.domain.UserRole;
import com.nerdcoffe.dto.UpvoteResponseDto;
import com.nerdcoffe.exception.EntityNotFoundException;
import com.nerdcoffe.repository.CommentRepository;
import com.nerdcoffe.repository.CommentUpvoteRepository;
import com.nerdcoffe.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentUpvoteRepository commentUpvoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public UpvoteResponseDto toggleUpvote(Long commentId) {
        log.info("Alternando upvote para comentário: {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comentário não encontrado com id: " + commentId));

        User currentUser = getCurrentUser();

        Optional<CommentUpvote> existingUpvote = commentUpvoteRepository.findByCommentAndUser(commentId, currentUser.getId());

        boolean upvoted;
        String message;

        if (existingUpvote.isPresent()) {
            commentUpvoteRepository.delete(existingUpvote.get());
            upvoted = false;
            message = "Upvote removido com sucesso";
            log.info("Upvote removido do comentário {} por usuário {}", commentId, currentUser.getId());
        } else {
            CommentUpvote newUpvote = CommentUpvote.builder()
                    .comment(comment)
                    .user(currentUser)
                    .build();
            commentUpvoteRepository.save(newUpvote);
            upvoted = true;
            message = "Upvote adicionado com sucesso";
            log.info("Upvote adicionado ao comentário {} por usuário {}", commentId, currentUser.getId());
        }

        Long upvoteCount = commentUpvoteRepository.countByCommentId(commentId);

        return UpvoteResponseDto.builder()
                .upvoted(upvoted)
                .upvoteCount(upvoteCount)
                .message(message)
                .build();
    }

    @Transactional
    public void deleteComment(Long commentId) {
        log.info("Deletando comentário: {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comentário não encontrado com id: " + commentId));

        User currentUser = getCurrentUser();

        if (!comment.getAuthor().getId().equals(currentUser.getId()) && !isAdmin()) {
            log.warn("Usuário não autorizado a deletar comentário: {}", commentId);
            throw new AccessDeniedException("Você não tem permissão para deletar este comentário");
        }

        commentUpvoteRepository.deleteByCommentId(commentId);
        commentRepository.delete(comment);
        log.info("Comentário deletado com sucesso: {}", commentId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = authentication.getName();
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new EntityNotFoundException("Usuário atual não encontrado"));
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
