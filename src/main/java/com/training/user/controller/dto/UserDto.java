package com.training.user.controller.dto;

import com.training.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class UserDto {

    private String login;
    private User.Role role;

    public UserDto(User user) {
        login = user.getLogin();
        role = user.getRole();
    }

    public static UserDto from(User user) {
        return UserDto.builder()
                .login(user.getLogin())
                .role(user.getRole())
                .build();
    }
}
