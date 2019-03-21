package com.training.todo.controller;

import com.training.Application;
import com.training.utils.TestJsonUtils;
import com.training.todo.domain.Todo;
import com.training.todo.persistance.TodoJpaRepository;
import com.training.utils.TestTodoUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.training.utils.FakeUsers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
@WebAppConfiguration
@SpringBootTest
@Slf4j
public class TodoRestControllerSecurityTest {

    private static final Todo PLAIN_USER_TODO = Todo.builder()
            .id(null)
            .name("todo")
            .username(PLAIN_USER.getLogin())
            .build();

    private static final Todo ADMIN_TODO = Todo.builder()
            .id(null)
            .name("todo admin")
            .username(ADMIN_USER.getLogin())
            .build();

    private static final Todo TODO = Todo.builder()
            .id(null)
            .name("todo")
            .username("other")
            .build();

    private static long lastId;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private TodoJpaRepository todoRepository;

    private long plainUserTodoId;
    private long adminTodoId;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void getAll_noCredentials_returnsUnauthorized401() throws Exception {
        // when
        mockMvc.perform(get(TestTodoUtils.TODOS_URL).with(anonymous()))
                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAll_authorizedUser_returnsOk() throws Exception {
        //when
        mockMvc.perform(get(TestTodoUtils.TODOS_URL).with(user(LOGGED_USER_PLAIN)))
                //then
                .andExpect(status().isOk());
    }

    @Test
    public void getAll_authorizedAdmin_returnsOk() throws Exception {
        //when
        mockMvc.perform(get(TestTodoUtils.TODOS_URL).with(user(LOGGED_USER_ADMIN)))
                //then
                .andExpect(status().isOk());
    }

    @Test
    public void create_noCredentials_returnsUnauthorized401() throws Exception {
        //when
        mockMvc.perform(post(TestTodoUtils.TODOS_URL)
                .with(anonymous())
                .contentType(TestJsonUtils.APPLICATION_JSON_UTF8)
                .content(TestJsonUtils.convertObjectToJson(TODO)))
                //then
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void create_userIsAuthorized_returnsCreated201() throws Exception {
        //when
        mockMvc.perform(post(TestTodoUtils.TODOS_URL)
                .with(user(LOGGED_USER_PLAIN))
                .contentType(TestJsonUtils.APPLICATION_JSON_UTF8)
                .content(TestJsonUtils.convertObjectToJson(TODO)))
                //then
                .andExpect(status().isCreated());
        todoRepository.delete(++lastId);
    }

    @Test
    public void get_noCredentials_returnsUnauthorized401() throws Exception {
        //given
        fillTheDatabaseWithTodos();
        //when
        mockMvc.perform(get(TestTodoUtils.todosUrlWithId(plainUserTodoId))
                .with(anonymous()))
                //then
                .andExpect(status().isUnauthorized());
        emptyTheDatabaseForNextTests();
    }

    @Test
    public void get_authorizedUserWantsToGetHisOwnTodo_returnsOk() throws Exception {
        //given
        fillTheDatabaseWithTodos();
        //when
        mockMvc.perform(get(TestTodoUtils.todosUrlWithId(plainUserTodoId))
                .with(user(LOGGED_USER_PLAIN)))
                //then
                .andExpect(status().isOk());
        emptyTheDatabaseForNextTests();
    }

    @Test
    public void get_authorizedAdminWantsToGetNotHisOwnTodo_returnsOk() throws Exception {
        //given
        fillTheDatabaseWithTodos();
        //when
        mockMvc.perform(get(TestTodoUtils.todosUrlWithId(plainUserTodoId))
                .with(user(LOGGED_USER_ADMIN)))
                //then
                .andExpect(status().isOk());
        emptyTheDatabaseForNextTests();
    }

    @Test
    public void get_authorizedUserWantsToGetNotHisOwnTodo_returnsForbidden403() throws Exception {
        //given
        fillTheDatabaseWithTodos();
        //when
        mockMvc.perform(get(TestTodoUtils.todosUrlWithId(adminTodoId))
                .with(user(LOGGED_USER_PLAIN)))
                //then
                .andExpect(status().isForbidden());
        emptyTheDatabaseForNextTests();
    }

    @Test
    public void update_noCredentials_returnsUnauthorized401() throws Exception {
        //when
        mockMvc.perform(put(TestTodoUtils.todosUrlWithId(1)).with(anonymous()))
                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void update_authorizedUserWantsToUpdateHisOwnTodo_returnsOk() throws Exception {
        //given
        fillTheDatabaseWithTodos();
        //when
        mockMvc.perform(put(TestTodoUtils.todosUrlWithId(plainUserTodoId))
                .with(user(LOGGED_USER_PLAIN))
                .contentType(TestJsonUtils.APPLICATION_JSON_UTF8)
                .content(TestJsonUtils.convertObjectToJson(TODO)))
                //then
                .andExpect(status().isOk());
        emptyTheDatabaseForNextTests();
    }

    private void fillTheDatabaseWithTodos() {
        plainUserTodoId = todoRepository.save(PLAIN_USER_TODO).getId();
        adminTodoId = todoRepository.save(ADMIN_TODO).getId();
        lastId = adminTodoId;
    }

    private void emptyTheDatabaseForNextTests() {
        try {
            todoRepository.delete(plainUserTodoId);
            todoRepository.delete(adminTodoId);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Todo already deleted.");
        }
    }

    @Test
    public void update_authorizedUserWantsToUpdateNotHisOwnTodo_returnsForbidden403() throws Exception {
        //given
        fillTheDatabaseWithTodos();
        //when
        mockMvc.perform(put(TestTodoUtils.todosUrlWithId(adminTodoId))
                .with(user(LOGGED_USER_PLAIN))
                .contentType(TestJsonUtils.APPLICATION_JSON_UTF8)
                .content(TestJsonUtils.convertObjectToJson(TODO)))
                //then
                .andExpect(status().isForbidden());
        emptyTheDatabaseForNextTests();
    }

    @Test
    public void update_authorizedAdminWantsToUpdateNotHisOwnTodo_returnsOk() throws Exception {
        //given
        fillTheDatabaseWithTodos();
        //when
        mockMvc.perform(put(TestTodoUtils.todosUrlWithId(plainUserTodoId))
                .with(user(LOGGED_USER_ADMIN))
                .contentType(TestJsonUtils.APPLICATION_JSON_UTF8)
                .content(TestJsonUtils.convertObjectToJson(TODO)))
                //then
                .andExpect(status().isOk());
        emptyTheDatabaseForNextTests();
    }

    @Test
    public void delete_noCredentials_returnsUnauthorized401() throws Exception {
        //when
        mockMvc.perform(delete(TestTodoUtils.todosUrlWithId(1)).with(anonymous()))
                // then
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void delete_authorizedUserWantsToDeleteHisOwnTodo_returnsNoContent204() throws Exception {
        //given
        fillTheDatabaseWithTodos();
        //when
        mockMvc.perform(delete(TestTodoUtils.todosUrlWithId(plainUserTodoId))
                .with(user(LOGGED_USER_PLAIN))
                .contentType(TestJsonUtils.APPLICATION_JSON_UTF8)
                .content(TestJsonUtils.convertObjectToJson(TODO)))
                //then
                .andExpect(status().isNoContent());
        emptyTheDatabaseForNextTests();
    }

    @Test
    public void delete_authorizedUserWantsToDeleteNotHisOwnTodo_returnsForbidden403() throws Exception {
        //given
        fillTheDatabaseWithTodos();
        //when
        mockMvc.perform(delete(TestTodoUtils.todosUrlWithId(adminTodoId))
                .with(user(LOGGED_USER_PLAIN))
                .contentType(TestJsonUtils.APPLICATION_JSON_UTF8)
                .content(TestJsonUtils.convertObjectToJson(TODO)))
                //then
                .andExpect(status().isForbidden());
        emptyTheDatabaseForNextTests();
    }

    @Test
    public void delete_authorizedAdminWantsToDeleteNotHisOwnTodo_returnsNoContent204() throws Exception {
        //given
        fillTheDatabaseWithTodos();
        //when
        mockMvc.perform(delete(TestTodoUtils.todosUrlWithId(plainUserTodoId))
                .with(user(LOGGED_USER_ADMIN))
                .contentType(TestJsonUtils.APPLICATION_JSON_UTF8)
                .content(TestJsonUtils.convertObjectToJson(TODO)))
                //then
                .andExpect(status().isNoContent());
        emptyTheDatabaseForNextTests();
    }

}