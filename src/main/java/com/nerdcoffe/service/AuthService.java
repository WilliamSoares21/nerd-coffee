package com.nerdcoffe.service;

import com.nerdcoffe.domain.User;
import com.nerdcoffe.domain.UserRole;
import com.nerdcoffe.dto.CreateUserDto;
import com.nerdcoffe.dto.LoginRequestDto;
import com.nerdcoffe.dto.LoginResponseDto;
import com.nerdcoffe.dto.UserDto;
import com.nerdcoffe.exception.BusinessException;
import com.nerdcoffe.exception.UnauthorizedException;
import com.nerdcoffe.repository.UserRepository;
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

    @Transactional
    public UserDto register(CreateUserDto dto) {
        log.info("Registrando novo usuário com email: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.warn("Email já existe: {}", dto.getEmail());
            throw new BusinessException("Email já cadastrado no sistema");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(UserRole.VIEWER)
                .active(true)
                .build();

        user = userRepository.save(user);
        log.info("Usuário registrado com sucesso: {}", user.getId());

        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto dto) {
        log.info("Tentativa de login para email: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email ou senha inválidos"));

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

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
