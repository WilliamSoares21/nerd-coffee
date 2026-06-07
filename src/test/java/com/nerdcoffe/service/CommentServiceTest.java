package com.nerdcoffe.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nerdcoffe.domain.Comment;
import com.nerdcoffe.domain.CommentUpvote;
import com.nerdcoffe.domain.User;
import com.nerdcoffe.domain.UserRole;
import com.nerdcoffe.dto.UpvoteResponseDto;
import com.nerdcoffe.exception.EntityNotFoundException;
import com.nerdcoffe.repository.CommentRepository;
import com.nerdcoffe.repository.CommentUpvoteRepository;
import com.nerdcoffe.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentUpvoteRepository commentUpvoteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldToggleUpvoteOnComment() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).build();
        Comment comment = Comment.builder().id(10L).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(commentUpvoteRepository.findByCommentAndUser(10L, 1L)).thenReturn(Optional.empty());
        when(commentUpvoteRepository.countByCommentId(10L)).thenReturn(1L);

        // Act
        UpvoteResponseDto response = commentService.toggleUpvote(10L);

        // Assert
        assertTrue(response.isUpvoted());
        assertEquals(1L, response.getUpvoteCount());
        assertEquals("Upvote adicionado com sucesso", response.getMessage());
        verify(commentUpvoteRepository, times(1)).save(any(CommentUpvote.class));
    }

    @Test
    void shouldToggleOffUpvoteOnComment() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).build();
        Comment comment = Comment.builder().id(10L).build();
        CommentUpvote upvote = CommentUpvote.builder().id(20L).comment(comment).user(user).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(commentUpvoteRepository.findByCommentAndUser(10L, 1L)).thenReturn(Optional.of(upvote));
        when(commentUpvoteRepository.countByCommentId(10L)).thenReturn(0L);

        // Act
        UpvoteResponseDto response = commentService.toggleUpvote(10L);

        // Assert
        assertFalse(response.isUpvoted());
        assertEquals(0L, response.getUpvoteCount());
        assertEquals("Upvote removido com sucesso", response.getMessage());
        verify(commentUpvoteRepository, times(1)).delete(upvote);
    }

    @Test
    void shouldDeleteCommentIfAuthor() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).build();
        Comment comment = Comment.builder().id(10L).author(user).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        // Act
        commentService.deleteComment(10L);

        // Assert
        verify(commentUpvoteRepository, times(1)).deleteByCommentId(10L);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void shouldDeleteCommentIfAdmin() {
        // Arrange
        String email = "admin@example.com";
        User admin = User.builder().id(2L).email(email).role(UserRole.ADMIN).build();
        User author = User.builder().id(1L).email("author@example.com").build();
        Comment comment = Comment.builder().id(10L).author(author).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(admin));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

        // Act
        commentService.deleteComment(10L);

        // Assert
        verify(commentUpvoteRepository, times(1)).deleteByCommentId(10L);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    void shouldThrowAccessDeniedWhenDeletingOtherUsersComment() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder().id(1L).email(email).role(UserRole.VIEWER).build();
        User author = User.builder().id(2L).email("author@example.com").build();
        Comment comment = Comment.builder().id(10L).author(author).build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_VIEWER"))).when(authentication).getAuthorities();

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> commentService.deleteComment(10L));
        verify(commentRepository, never()).delete(any(Comment.class));
    }
}
