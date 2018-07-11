package com.impaqgroup.training.security.service;

import com.impaqgroup.training.security.model.CurrentUser;
import com.impaqgroup.training.user.persistance.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserProvider {

    private UserRepository userRepository;

    @Autowired
    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getCurrentUserName() {
        return getCurrentUser().getUsername();
    }

    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (CurrentUser) authentication.getPrincipal();
    }

    public boolean isAdminLoggedIn() {
        return getCurrentUser().getRole().equals("ROLE_ADMIN");
    }
}