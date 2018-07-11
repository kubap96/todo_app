package com.impaqgroup.training.user.controller;

import com.impaqgroup.training.Application;
import com.impaqgroup.training.user.domain.User;
import com.impaqgroup.training.user.persistance.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.impaqgroup.training.utils.FakeUsers.LOGGED_USER_ADMIN;
import static com.impaqgroup.training.utils.FakeUsers.LOGGED_USER_PLAIN;
import static com.impaqgroup.training.utils.TestJsonUtils.APPLICATION_JSON_UTF8;
import static com.impaqgroup.training.utils.TestJsonUtils.convertObjectToJson;
import static com.impaqgroup.training.user.domain.User.Role.ADMIN;
import static com.impaqgroup.training.user.domain.User.Role.USER;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
@WebAppConfiguration
@SpringBootTest
public class UserRestControllerSecurityTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String USERS_URL = "/users";

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void getAll_noCredentials_returnsUnauthorized401() throws Exception {
        //when
        mockMvc.perform(get(USERS_URL).with(anonymous()))
                //then
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAll_authorizedPlainUser_returnsForbidden403() throws Exception {
        //when
        mockMvc.perform(get(USERS_URL)
                .with(user("user").roles(USER.toString())))
                //then
                .andExpect(status().isForbidden());
    }

    @Test
    public void getAll_authorizedAdminUser_returnsOk() throws Exception {
        //when
        mockMvc.perform(get(USERS_URL)
                .with(user("admin").roles(ADMIN.toString())))
                //then
                .andExpect(status().isOk());
    }

    @Test
    public void post_noCredentials_returnsUnauthorized401() throws Exception {
        //when
        mockMvc.perform(post(USERS_URL).with(anonymous()))
                //then
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void post_authorizedPlainUser_returnsForbidden403() throws Exception {
        //when
        mockMvc.perform(post(USERS_URL)
                .with(user("user").roles(USER.toString())))
                //then
                .andExpect(status().isForbidden());
    }

    @Test
    public void post_authorizedAdminUser_returnsOk() throws Exception {
        //when
        mockMvc.perform(get(USERS_URL)
                .with(user("admin").roles(ADMIN.toString())))
                //then
                .andExpect(status().isOk());
    }

    @Test
    public void delete_authorizedAdminUser_returnsNoContent204() throws Exception {
        //when
        mockMvc.perform(delete(USERS_URL + "/henio")
                .with(user(LOGGED_USER_ADMIN)))
                //then
                .andExpect(status().isNoContent());
        restoreStateBeforeDelete();
    }

    private void restoreStateBeforeDelete() {
        userRepository.save(User.builder()
                .login("henio")
                .passwordHash(passwordEncoder.encode("henio"))
                .role(User.Role.USER)
                .build());
    }

    @Test
    public void delete_authorizedPlainUser_returnsForbidden403() throws Exception {
        //when
        mockMvc.perform(delete(USERS_URL + "/henio")
                .with(user(LOGGED_USER_PLAIN)))
                //then
                .andExpect(status().isForbidden());
    }

    @Test
    public void delete_noCredentials_returnsUnauthorized401() throws Exception {
        //when
        mockMvc.perform(delete(USERS_URL + "/henio")
                .with(anonymous()))
                //then
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void resetPassword_authorizedPlainUser_returnsForbidden403() throws Exception {
        //when
        mockMvc.perform(post(USERS_URL + "/henio/reset-password")
                .with(user(LOGGED_USER_PLAIN)))
                //then
                .andExpect(status().isForbidden());
    }

    @Test
    public void resetPassword_noCredentials_returnsUnauthorized401() throws Exception {
        //when
        mockMvc.perform(post(USERS_URL + "/henio/reset-password")
                .with(anonymous()))
                //then
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void resetPassword_authorizedAdminUser_returnsOk() throws Exception {
        //when
        mockMvc.perform(post(USERS_URL + "/henio/reset-password")
                .with(user(LOGGED_USER_ADMIN)))
                //then
                .andExpect(status().isOk());
    }

    @Test
    public void updateRole_authorizedPlainUser_returnsForbidden403() throws Exception {
        //when
        mockMvc.perform(patch(USERS_URL + "/henio/role")
                .with(user(LOGGED_USER_PLAIN))
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(User.Role.ADMIN)))
                //then
                .andExpect(status().isForbidden());
    }

    @Test
    public void updateRole_noCredentials_returnsUnauthorized401() throws Exception {
        //when
        mockMvc.perform(patch(USERS_URL + "/henio/role")
                .with(anonymous())
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(User.Role.ADMIN)))
                //then
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateRole_authorizedAdminUser_returnsOk() throws Exception {
        //when
        mockMvc.perform(patch(USERS_URL + "/henio/role")
                .with(user(LOGGED_USER_ADMIN))
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(User.Role.ADMIN)))
                //then
                .andExpect(status().isOk());
    }

    @Test
    public void updatePassword_noCredentials_returnsUnauthorized401() throws Exception {
        //when
        mockMvc.perform(patch(USERS_URL + "/henio/password")
                .with(anonymous())
                .contentType(APPLICATION_JSON_UTF8)
                .content("password"))
                //then
                .andExpect(status().isUnauthorized());
    }
}