package com.training.user.service;

import com.training.security.service.CurrentUserProvider;
import com.training.todo.utils.ForbiddenException;
import com.training.user.controller.dto.User2UserDtoConverter;
import com.training.user.controller.dto.UserDto;
import com.training.user.domain.User;
import com.training.user.persistance.UserRepository;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

@Service
public class UserService {

    private static final int DEFAULT_PASSWORD_LENGTH = 16;

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private CurrentUserProvider currentUserProvider;
    private User2UserDtoConverter converter;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserProvider = currentUserProvider;
        this.converter = new User2UserDtoConverter();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserDto> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(converter);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserDto create(User user) {
        isExisting(user);
        user.encodePassword(passwordEncoder);
        User created = userRepository.save(user);
        return UserDto.from(created);
    }

    private void isExisting(User user) {
        if (exists(user)) {
            throw new EntityExistsException("User with login " +
                    user.getLogin() + " already exists.");
        }
    }

    private boolean exists(User user) {
        return userRepository.exists(user.getLogin());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(String login) {
        isRemovable(login);
        tryToDelete(login);
    }

    private void isRemovable(String login) {
        if (cannotDelete(login)) {
            throw new ForbiddenException("Admin can not delete his own account.");
        }
    }

    private boolean cannotDelete(String login) {
        return currentUserProvider.getCurrentUserName().equals(login);
    }

    private boolean isLoggedIn(String login) {
        return currentUserProvider.getCurrentUserName().equals(login);
    }

    private void tryToDelete(String login) {
        try {
            userRepository.delete(login);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public String resetPassword(String login) {
        checkExist(login);
        String password = generatePassword();
        String passwordHash = passwordEncoder.encode(password);
        userRepository.updatePassword(login, passwordHash);
        return password;
    }

    private void checkExist(String login) {
        if (notExist(login)) {
            throw new EntityNotFoundException();
        }
    }

    private boolean notExist(String login) {
        return !userRepository.exists(login);
    }

    private String generatePassword() {
        RandomStringGenerator generator = new RandomStringGenerator.Builder()
                .withinRange('0', 'z')
                .build();
        return generator.generate(DEFAULT_PASSWORD_LENGTH);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateRole(String login, User.Role role) {
        checkExist(login);
        checkCanUpdateRole(login);
        userRepository.updateRole(login, role);
    }

    private void checkCanUpdateRole(String login) {
        if (cannotUpdateRole(login)) {
            throw new ForbiddenException("Admin could not update his own account");
        }
    }

    private boolean cannotUpdateRole(String login) {
        return isLoggedIn(login);
    }

    @PreAuthorize("isAuthenticated()")
    public void updatePassword(String login, String password) {
        checkExist(login);
        if (isLoggedIn(login)) {
            userRepository.updatePassword(login,
                    passwordEncoder.encode(password));
        } else {
            throw new ForbiddenException("Could not change other user password");
        }
    }
}
