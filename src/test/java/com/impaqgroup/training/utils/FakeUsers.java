package com.impaqgroup.training.utils;

import com.impaqgroup.training.security.model.CurrentUser;
import com.impaqgroup.training.user.controller.dto.UserDto;
import com.impaqgroup.training.user.domain.User;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;

public class FakeUsers {

    public static final User PLAIN_USER = User.builder()
            .login("henio")
            .passwordHash("henio")
            .role(User.Role.USER)
            .todos(emptyList())
            .build();

    public static final User ADMIN_USER = User.builder()
            .login("admin")
            .passwordHash("admin")
            .role(User.Role.ADMIN)
            .todos(emptyList())
            .build();

    public static final ArrayList<User> ALL_USERS = newArrayList(PLAIN_USER, ADMIN_USER);

    public static final ArrayList<UserDto> ALL_USERS_DTO = newArrayList(
            UserDto.from(PLAIN_USER), UserDto.from(ADMIN_USER));

    private static final String ROLE_PREFIX = "ROLE_";

    public static final CurrentUser LOGGED_USER_ADMIN = CurrentUser.builder()
            .login(ADMIN_USER.getLogin())
            .passwordHash(ADMIN_USER.getPasswordHash())
            .role(ROLE_PREFIX + ADMIN_USER.getRole().toString())
            .build();

    public static final CurrentUser LOGGED_USER_PLAIN = CurrentUser.builder()
            .login(PLAIN_USER.getLogin())
            .passwordHash(PLAIN_USER.getPasswordHash())
            .role(ROLE_PREFIX + PLAIN_USER.getRole().toString())
            .build();
}