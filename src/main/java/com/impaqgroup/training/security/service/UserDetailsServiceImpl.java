package com.impaqgroup.training.security.service;

import com.impaqgroup.training.security.model.CurrentUser;
import com.impaqgroup.training.user.persistance.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return Optional.ofNullable(userRepository.findOne(username))
                .map(user -> CurrentUser.builder()
                        .login(user.getLogin())
                        .passwordHash(user.getPasswordHash())
                        .role(ROLE_PREFIX + user.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
