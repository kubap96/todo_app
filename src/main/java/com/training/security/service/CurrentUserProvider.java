package com.training.security.service;

import com.training.security.model.CurrentUser;
import com.training.user.persistance.UserRepository;
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