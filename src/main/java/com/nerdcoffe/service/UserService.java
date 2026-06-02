package com.nerdcoffe.service;

import com.nerdcoffe.domain.User;
import com.nerdcoffe.dto.UserProfileDto;
import com.nerdcoffe.exception.EntityNotFoundException;
import com.nerdcoffe.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String username) {
        log.info("Buscando perfil do usuário: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com username: " + username));

        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getEmail())
                .avatarUrl(null)
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
