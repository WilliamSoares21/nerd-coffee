package com.nerdcoffe.service;

import com.nerdcoffe.domain.User;
import com.nerdcoffe.domain.UserRole;
import com.nerdcoffe.domain.VerificationToken;
import com.nerdcoffe.dto.CreateUserDto;
import com.nerdcoffe.dto.LoginRequestDto;
import com.nerdcoffe.dto.LoginResponseDto;
import com.nerdcoffe.dto.UserDto;
import com.nerdcoffe.exception.BusinessException;
import com.nerdcoffe.exception.ConflictException;
import com.nerdcoffe.exception.UnauthorizedException;
import com.nerdcoffe.exception.UnverifiedAccountException;
import com.nerdcoffe.repository.UserRepository;
import com.nerdcoffe.repository.VerificationTokenRepository;
import com.nerdcoffe.security.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public UserDto register(CreateUserDto dto) {
        log.info("Registrando novo usuário com email: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Email já existe: {}", dto.getEmail());
            throw new ConflictException("Usuário com email já cadastrado");
        }

        String baseUsername = dto.getEmail().split("@")[0].replaceAll("[^a-zA-Z0-9_.-]", "");
        if (baseUsername.isEmpty()) {
            baseUsername = "user";
        }
        if (baseUsername.length() > 40) {
            baseUsername = baseUsername.substring(0, 40);
        }

        String generatedUsername;
        int attempts = 0;
        boolean unique;
        do {
            generatedUsername = baseUsername.toLowerCase() + "_" + java.util.UUID.randomUUID().toString().substring(0, 4);
            unique = !userRepository.existsByUsername(generatedUsername);
            attempts++;
        } while (!unique && attempts < 5);

        if (!unique) {
            log.warn("Falha ao gerar username único para base: {}", baseUsername);
            throw new RuntimeException("Falha ao gerar username único");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.VIEWER)
                .active(true)
                .username(generatedUsername)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("Usuário registrado com sucesso: {}", user.getId());

        String token = java.util.UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(java.time.LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);

        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto dto) {
        log.info("Tentativa de login para email: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email ou senha inválidos"));

        if (!user.isEmailVerified()) {
            throw new UnverifiedAccountException("Conta não verificada. Verifique seu e-mail.");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            log.warn("Senha inválida para usuário: {}", dto.getEmail());
            throw new UnauthorizedException("Email ou senha inválidos");
        }

        if (!user.isEnabled()) {
            log.warn("Usuário desativado: {}", dto.getEmail());
            throw new UnauthorizedException("Usuário desativado");
        }

        // Carrega o UserDetails através do Spring Security para garantir
        // que a interface UserDetails seja respeitada pelo JwtProvider
        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getEmail());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        
        String token = jwtProvider.generateToken(userDetails);
        log.info("Login bem-sucedido para usuário: {}", user.getId());

        return LoginResponseDto.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtProvider.getExpirationMs())
                .userId(user.getId())
                .roles(roles)
                .user(mapToDto(user))
                .build();
    }

    @Transactional
    public void verifyEmail(String token) {
        log.info("Verificando e-mail com token: {}", token);
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Token inválido ou inexistente"));

        if (verificationToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new BusinessException("Token expirado");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
        log.info("E-mail verificado com sucesso para o usuário: {}", user.getEmail());
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .username(user.getUsername())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
