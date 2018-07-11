package com.impaqgroup.training.user.controller;

import com.impaqgroup.training.user.controller.dto.UserDto;
import com.impaqgroup.training.user.domain.User;
import com.impaqgroup.training.user.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;

import static com.google.common.collect.Lists.newArrayList;
import static com.impaqgroup.training.utils.FakeUsers.*;
import static com.impaqgroup.training.utils.TestJsonUtils.APPLICATION_JSON_UTF8;
import static com.impaqgroup.training.utils.TestJsonUtils.convertObjectToJson;
import static com.impaqgroup.training.user.domain.User.Role.ADMIN;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class StandaloneUserRestControllerTest {

    private static final String USERS_URL = "/users";
    private static final String RESET_PASSWORD_URL = "/reset-password";
    private static final String ROLE_URL = "/role";
    private static final String PASSWORD_URL = "/password";

    private MockMvc mockMvc;

    @Mock
    private UserService fakeService;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(new UserRestController(fakeService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    public void getAll_properRequest_returns200() throws Exception {
        //given
        when( fakeService.getAllUsers(any(Pageable.class)))
                .thenReturn(new PageImpl<>(emptyList()));
        //when
        mockMvc.perform(get(USERS_URL))
                //then
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void getAllUsers_usersFound_returnsUsers() throws Exception {
        // given
        when( fakeService.getAllUsers(any(Pageable.class)))
                .thenReturn(new PageImpl<>(ALL_USERS_DTO));
        // then
        mockMvc.perform(get(USERS_URL))
                .andExpect(jsonPath("$['content']", hasSize(ALL_USERS_DTO.size())))
                .andExpect(jsonPath("$['content'].[*].login", hasItem(PLAIN_USER.getLogin())))
                .andExpect(jsonPath("$['content'].[*].login", hasItem(ADMIN_USER.getLogin())));
    }

    @Test
    public void getAllUsers_usersFound_returnsSameUserButWithoutPassword() throws Exception {
        // given
        when( fakeService.getAllUsers(any(Pageable.class)))
                .thenReturn(new PageImpl<>(newArrayList(UserDto.from(PLAIN_USER))));
        // then
        mockMvc.perform(get(USERS_URL))
                .andExpect(jsonPath("$['content']", hasSize(1)))
                .andExpect(jsonPath("$['content'].[0].login", is(PLAIN_USER.getLogin())))
                .andExpect(jsonPath("$['content'].[0].role", is(PLAIN_USER.getRole().toString())))
                .andExpect(jsonPath("$['content'].[0].passwordHash").doesNotExist());
    }


    @Test
    public void getAllUsers_nothingFound_returnsEmptyList() throws Exception {
        // given
        when( fakeService.getAllUsers(any(Pageable.class)))
                .thenReturn(new PageImpl<>(emptyList()));
        // then
        mockMvc.perform(get(USERS_URL)).andExpect(jsonPath("$['content']", hasSize(0)));
    }

    @Test
    public void post_properRequest_returnsCreated201() throws Exception {
        // given
        when(fakeService.create(PLAIN_USER))
                .thenAnswer(invocation -> UserDto.from(user(invocation.getArguments()[0])));
        // when
        mockMvc.perform(post(USERS_URL)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(PLAIN_USER)))
                // then
                .andExpect(status().isCreated());
    }

    private User user(Object o) {
        return (User) o;
    }

    @Test
    public void post_properRequest_returnsUserDto() throws Exception {
        // given
        when(fakeService.create(PLAIN_USER))
                .thenAnswer(invocation -> UserDto.from(user(invocation.getArguments()[0])));
        // when
        mockMvc.perform(post(USERS_URL)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(PLAIN_USER)))
                // then
                .andExpect(jsonPath("$.login", is(PLAIN_USER.getLogin())))
                .andExpect(jsonPath("$.role", is(PLAIN_USER.getRole().toString())))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    public void post_elementNotValid_returns400() throws Exception {
        // when
        mockMvc.perform(post(USERS_URL)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(UserDto.builder()
                        .role(ADMIN).build())))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    public void delete_userCanBeDeleted_returnsNoContent204() throws Exception {
        // when
        mockMvc.perform(delete(USERS_URL + "/henio"))
                // then
                .andExpect(status().isNoContent());
    }

    @Test
    public void delete_userNotFound_returnsNotFound404() throws Exception {
        // given
        doThrow(EntityNotFoundException.class)
                .when(fakeService).delete("user");
        // when
        mockMvc.perform(delete(USERS_URL + "/user"))
                // then
                .andExpect(status().isNotFound());
    }

    @Test
    public void resetPassword_userFound_returnsGeneratedPasswordAndOk200() throws Exception {
        // given
        String expectedPassword = "password";
        when(fakeService.resetPassword("user")).thenReturn(expectedPassword);
        // when
        mockMvc.perform(post(USERS_URL + "/user" + RESET_PASSWORD_URL))
                // then
                .andExpect(status().isOk())
                .andExpect(content().string(expectedPassword));
    }

    @Test
    public void resetPassword_userNotFound_returnsNotFound404() throws Exception {
        // given
        doThrow(EntityNotFoundException.class)
                .when(fakeService).resetPassword("user");
        // when
        mockMvc.perform(post(USERS_URL + "/user" + RESET_PASSWORD_URL))
                // then
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateRole_roleCanBeChanged_returnsOk() throws Exception {
        // when
        mockMvc.perform(patch(USERS_URL + "/henio" + ROLE_URL)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(User.Role.ADMIN)))
                // then
                .andExpect(status().isOk());
    }

    @Test
    public void updateRole_userNotFound_returnsNotFound404() throws Exception {
        // given
        doThrow(EntityNotFoundException.class)
                .when(fakeService).updateRole("henio", User.Role.ADMIN);
        // when
        mockMvc.perform(patch(USERS_URL + "/henio" + ROLE_URL)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(User.Role.ADMIN)))
                // then
                .andExpect(status().isNotFound());
    }

    @Test
    public void updatePassword_passwordCanBeChanged_returnsOk() throws Exception {
        // when
        mockMvc.perform(patch(USERS_URL + "/henio" + PASSWORD_URL)
                .contentType(APPLICATION_JSON_UTF8)
                .content("password"))
                // then
                .andExpect(status().isOk());
    }

    @Test
    public void updatePassword_userNotFound_returnsNotFound404() throws Exception {
        // given
        doThrow(EntityNotFoundException.class)
                .when(fakeService).updatePassword("henio", "newpassword123");
        // when
        mockMvc.perform(patch(USERS_URL + "/henio" + PASSWORD_URL)
                .contentType(APPLICATION_JSON_UTF8)
                .content("newpassword123"))
                // then
                .andExpect(status().isNotFound());
    }

}