package com.nerdcoffe.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nerdcoffe.domain.User;
import com.nerdcoffe.domain.UserRole;
import com.nerdcoffe.domain.VerificationToken;
import com.nerdcoffe.dto.CreateUserDto;
import com.nerdcoffe.dto.UserDto;
import com.nerdcoffe.exception.ConflictException;
import com.nerdcoffe.exception.UnverifiedAccountException;
import com.nerdcoffe.exception.BusinessException;
import com.nerdcoffe.repository.UserRepository;
import com.nerdcoffe.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterSuccessfullyOnFirstAttempt() {
        CreateUserDto dto = CreateUserDto.builder()
                .name("John Doe")
                .email("John@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            return savedUser;
        });

        UserDto result = authService.register(dto);

        assertNotNull(result);
        assertTrue(result.getUsername().startsWith("john_"));
        verify(userRepository, times(1)).existsByUsername(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
        verify(emailService, times(1)).sendVerificationEmail(eq(dto.getEmail()), anyString());
    }

    @Test
    void shouldRegisterSuccessfullyAfterMultipleAttempts() {
        CreateUserDto dto = CreateUserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        // First 2 times username exists, 3rd time it doesn't
        when(userRepository.existsByUsername(anyString()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = authService.register(dto);

        assertNotNull(result);
        verify(userRepository, times(3)).existsByUsername(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(verificationTokenRepository, times(1)).save(any(VerificationToken.class));
        verify(emailService, times(1)).sendVerificationEmail(eq(dto.getEmail()), anyString());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenAllAttemptsFail() {
        CreateUserDto dto = CreateUserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        // All 5 attempts say username exists
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.register(dto)
        );

        assertEquals("Falha ao gerar username único", exception.getMessage());
        verify(userRepository, times(5)).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowConflictExceptionWhenEmailExists() {
        CreateUserDto dto = CreateUserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> authService.register(dto)
        );

        assertEquals("Usuário com email já cadastrado", exception.getMessage());
        verify(userRepository, never()).existsByUsername(anyString());
    }

    @Test
    void shouldVerifyEmailSuccessfully() {
        String token = "valid-token";
        User user = User.builder()
                .email("test@example.com")
                .emailVerified(false)
                .build();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

        authService.verifyEmail(token);

        assertTrue(user.isEmailVerified());
        verify(userRepository, times(1)).save(user);
        verify(verificationTokenRepository, times(1)).delete(verificationToken);
    }

    @Test
    void shouldThrowBusinessExceptionWhenVerifyEmailTokenNotFound() {
        String token = "invalid-token";
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.verifyEmail(token)
        );

        assertEquals("Token inválido ou inexistente", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowBusinessExceptionWhenVerifyEmailTokenExpired() {
        String token = "expired-token";
        User user = User.builder()
                .email("test@example.com")
                .emailVerified(false)
                .build();

        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .build();

        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.of(verificationToken));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.verifyEmail(token)
        );

        assertEquals("Token expirado", exception.getMessage());
        verify(verificationTokenRepository, times(1)).delete(verificationToken);
        verify(userRepository, never()).save(any(User.class));
    }
}
