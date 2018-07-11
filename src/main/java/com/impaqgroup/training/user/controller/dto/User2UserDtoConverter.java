package com.impaqgroup.training.user.controller.dto;

import com.impaqgroup.training.user.domain.User;
import org.springframework.core.convert.converter.Converter;

public class User2UserDtoConverter implements Converter<User, UserDto> {
    @Override
    public UserDto convert(User user) {
        return new UserDto(user.getLogin(), user.getRole());
    }
}
