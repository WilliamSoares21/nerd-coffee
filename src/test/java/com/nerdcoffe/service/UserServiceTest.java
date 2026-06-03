package com.nerdcoffe.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nerdcoffe.domain.User;
import com.nerdcoffe.domain.UserRole;
import com.nerdcoffe.dto.UpdateProfileDto;
import com.nerdcoffe.dto.UserProfileDto;
import com.nerdcoffe.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

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
    void shouldUpdateProfileSuccessfully() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .name("Old Name")
                .email(email)
                .password("password")
                .role(UserRole.VIEWER)
                .bio("Old Bio")
                .avatarUrl("old-avatar.png")
                .build();

        UpdateProfileDto dto = UpdateProfileDto.builder()
                .name("New Name")
                .bio("New Bio")
                .avatarUrl("new-avatar.png")
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserProfileDto result = userService.updateProfile(dto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Name", result.getName());
        assertEquals("New Bio", result.getBio());
        assertEquals("new-avatar.png", result.getAvatarUrl());
        
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void shouldUpdateOnlyFilledFields() {
        // Arrange
        String email = "test@example.com";
        User user = User.builder()
                .id(1L)
                .name("Old Name")
                .email(email)
                .password("password")
                .role(UserRole.VIEWER)
                .bio("Old Bio")
                .avatarUrl("old-avatar.png")
                .build();

        UpdateProfileDto dto = UpdateProfileDto.builder()
                .name("New Name")
                // bio and avatarUrl are null
                .build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserProfileDto result = userService.updateProfile(dto);

        // Assert
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("Old Bio", result.getBio()); // remains unchanged
        assertEquals("old-avatar.png", result.getAvatarUrl()); // remains unchanged
    }
}
