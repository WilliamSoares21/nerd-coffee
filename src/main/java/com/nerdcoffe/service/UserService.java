package com.nerdcoffe.service;

import com.nerdcoffe.domain.User;
import com.nerdcoffe.dto.UpdateProfileDto;
import com.nerdcoffe.dto.UserProfileDto;
import com.nerdcoffe.exception.EntityNotFoundException;
import com.nerdcoffe.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String username) {
        log.debug("Buscando perfil de utilizador");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com username: " + username));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .username(isAuthenticated ? user.getEmail() : null)
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public UserProfileDto updateProfile(UpdateProfileDto dto) {
        log.info("Atualizando perfil do usuário logado");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.security.authentication.BadCredentialsException("Usuário não autenticado");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com email: " + email));

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getAvatarUrl() != null) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }

        user = userRepository.save(user);

        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
