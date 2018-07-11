package com.impaqgroup.training.todo.controller;

import com.impaqgroup.training.todo.domain.Todo;
import com.impaqgroup.training.todo.service.TodoService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static com.impaqgroup.training.utils.TestJsonUtils.APPLICATION_JSON_UTF8;
import static com.impaqgroup.training.utils.TestJsonUtils.convertObjectToJson;
import static com.impaqgroup.training.utils.TestTodoUtils.TODOS_URL;
import static com.impaqgroup.training.utils.TestTodoUtils.todosUrlWithId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;


@RunWith(SpringJUnit4ClassRunner.class)
public class StandaloneTodoRestControllerTest {

    private static final Todo TODO = new Todo(-1L, "nazwa", "priorytet", "opis", true, "user");
    private static final Todo TODO_2 = new Todo(-1L, "nazwa2", "priorytet2", "opis2", true, "user");

    private static final Todo WITHOUT_NAME = Todo.builder()
            .priority("wazny")
            .description("opis")
            .completed(false)
            .build();

    private static final ArrayList<Todo> TODOS = newArrayList(TODO, TODO_2);

    private static final long ID_FROM_URL = 1;

    private static final Long CREATED_ID = 1L;

    private static final ArgumentCaptor<Todo> ARGUMENT = ArgumentCaptor.forClass(Todo.class);

    private MockMvc mockMvc;

    @Mock
    private TodoService todoService;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        mockMvc = standaloneSetup(new TodoRestController(todoService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    public void getAll_properRequest_returns200() throws Exception {
        //given
        when( todoService.getAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(TODOS));
        //when
        mockMvc.perform(get(TODOS_URL))
                //then
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8));
    }

    @Test
    public void getAll_todosFound_returnsTodos() throws Exception {
        //given
        when( todoService.getAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(TODOS));
        //when
        mockMvc.perform(get(TODOS_URL))
                //then
                .andExpect(jsonPath("$['content']", hasSize(2)))
                .andExpect(jsonPath("$['content'].[*].name", hasItem(TODO.getName())))
                .andExpect(jsonPath("$['content'].[*].name", hasItem(TODO_2.getName())));
    }

    @Test
    public void getAll_todoFound_returnsTodoWithAllProperties() throws Exception {
        // given
        when( todoService.getAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(newArrayList(TODO_2)));
        // when
        mockMvc.perform(get(TODOS_URL))
                // then
                .andExpect(jsonPath("$['content']", hasSize(1)))
                .andExpect(jsonPath("$['content'].[0].id", is(TODO_2.getId().intValue())))
                .andExpect(jsonPath("$['content'].[0].name", is(TODO_2.getName())))
                .andExpect(jsonPath("$['content'].[0].priority", is(TODO_2.getPriority())))
                .andExpect(jsonPath("$['content'].[0].description", is(TODO_2.getDescription())))
                .andExpect(jsonPath("$['content'].[0].completed", is(TODO_2.isCompleted())))
                .andExpect(jsonPath("$['content'].[0].username", is(TODO_2.getUsername())));
    }

    @Test
    public void post_elementWithoutName_returns400() throws Exception {
        //when
        mockMvc.perform(post(TODOS_URL)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(WITHOUT_NAME)))
                //then
                .andExpect(status().is4xxClientError());
        verify(todoService, times(0)).create(any(Todo.class));
    }

    @Test
    public void post_properRequest_returnsCreatedIdAnd201() throws Exception {
        // given
        when( todoService.create(Matchers.isA(Todo.class)))
                .thenReturn(CREATED_ID);
        // when
        mockMvc.perform(post(TODOS_URL)
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(TODO)))
                // then
                .andExpect(status().isCreated())
                .andExpect(content().string(CREATED_ID.toString()));
        verify( todoService).create(ARGUMENT.capture());
        assertThatTodosAreEqualExceptId(ARGUMENT.getValue(), TODO);
    }

    private void assertThatTodosAreEqualExceptId(Todo expected, Todo actual) {
        assertThat(expected.getName()).isEqualTo(actual.getName());
        assertThat(expected.getPriority()).isEqualTo(actual.getPriority());
        assertThat(expected.getDescription()).isEqualTo(actual.getDescription());
        assertThat(expected.isCompleted()).isEqualTo(actual.isCompleted());
        assertThat(expected.getUsername()).isEqualTo(actual.getUsername());
    }

    @Test
    public void put_elementWithoutName_returns400() throws Exception {
        // when
        mockMvc.perform(put(todosUrlWithId(1))
                .contentType(APPLICATION_JSON_UTF8)
                .content(convertObjectToJson(WITHOUT_NAME)))
                // then
                .andExpect(status().is4xxClientError());
        verify( todoService, times(0)).update(any(Todo.class));
    }

    @Test
    public void put_properRequest_updatesTodoAndReturns200() throws Exception {
        // given
        String givenTodoJson = convertObjectToJson(withoutId().build());
        Todo expectedTodo = withoutId().id(ID_FROM_URL).build();
        // when
        mockMvc.perform(put(todosUrlWithId(ID_FROM_URL))
                .contentType(APPLICATION_JSON_UTF8)
                .content(givenTodoJson))
                // then
                .andExpect(status().isOk());
        verify(todoService).update(eq(expectedTodo));
    }

    private Todo.TodoBuilder withoutId() {
        return Todo.builder()
                .name("update")
                .priority("update")
                .description("Rozwiązać zadanie 2")
                .completed(false);
    }

    @Test
    public void delete_todoFound_deletesTodoAndReturns204() throws Exception {
        // when
        mockMvc.perform(delete(todosUrlWithId(ID_FROM_URL)))
                // then
                .andExpect(status().isNoContent());
        verify(todoService).delete(ID_FROM_URL);
    }

    @Test
    public void get_todoFound_returnsTodoWithoutNullFields() throws Exception {
        // given
        Todo todo = new Todo(-1L, "nazwa2", "priorytet2", "opis2", true, null);
        when( todoService.get(ID_FROM_URL)).thenReturn(todo);
        // when
        mockMvc.perform(get(todosUrlWithId(ID_FROM_URL)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(todo.getId().intValue())))
                .andExpect(jsonPath("$.name", is(todo.getName())))
                .andExpect(jsonPath("$.priority", is(todo.getPriority())))
                .andExpect(jsonPath("$.description", is(todo.getDescription())))
                .andExpect(jsonPath("$.completed", is(todo.isCompleted())))
                .andExpect(jsonPath("$.username").doesNotExist());
    }
}