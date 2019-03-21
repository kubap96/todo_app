package com.training.todo.service;


import com.training.security.service.CurrentUserProvider;
import com.training.todo.domain.Todo;
import com.training.todo.persistance.TodoJpaRepository;
import com.training.todo.utils.ForbiddenException;
import com.training.utils.FakeUsers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;

import javax.persistence.EntityNotFoundException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TodoServiceTest {

    private static final long ID = 1L;

    private static final Todo PLAIN_USER_TODO = new Todo(
            ID, "todo1", "priority1", "about todo1", true, FakeUsers.PLAIN_USER.getLogin());

    private static final Todo ADMIN_USER_TODO = new Todo(
            ID, "todo1", "priority1", "about todo1", true, FakeUsers.ADMIN_USER.getLogin());

    private static final List<Todo> FAKE_TODOS = newArrayList(
            PLAIN_USER_TODO,
            new Todo(2L, "todo2", "priority2", "about todo2", true, "username")
    );


    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private TodoJpaRepository fakeRepository;

    @InjectMocks
    private TodoService todoService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void hasAccessToTodo_userIsLoggedIn_returnsTrueForOwnTodoAndFalseForOthers() {
        // given
        plainUserIsLoggedIn();
        // when
        boolean hasAccessToOwn = todoService.hasAccessTo(PLAIN_USER_TODO);
        // then
        assertTrue(hasAccessToOwn);
    }

    private void plainUserIsLoggedIn() {
        when(currentUserProvider.isAdminLoggedIn()).thenReturn(false);
        when(currentUserProvider.getCurrentUserName()).thenReturn(FakeUsers.LOGGED_USER_PLAIN.getLogin());
    }

    @Test
    public void hasAccessToTodo_userIsLoggedIn_returnsFalseForOthers() {
        // given
        plainUserIsLoggedIn();
        Todo othersTodo = new Todo(1L, "todo1", "priority1", "about todo1", true, "other");
        // when
        boolean hasAccessToOthers = todoService.hasAccessTo(othersTodo);
        // then
        assertFalse(hasAccessToOthers);
    }

    private String firstElementUsername(Page<Todo> todos) {
        return todos.getContent().get(0).getUsername();
    }

    @Test
    public void create_userIsLoggedIn_savesGivenTodoWithLoggedUserNameAndReturnsCreatedId() {
        // given
        Todo given = new Todo(null, "todo", "high", "", false, null);
        when(currentUserProvider.getCurrentUserName()).thenReturn(FakeUsers.LOGGED_USER_PLAIN.getLogin());
        when(fakeRepository.save(eq(withLoggedUserName(given)))).thenReturn(withCreatedId(given));
        // when
        Long createdId = todoService.create(given);
        // then
        assertThat(createdId).isEqualTo(ID);
        verify(fakeRepository).save(withLoggedUserName(given));
    }

    private Todo withLoggedUserName(Todo todo) {
        return Todo.builder()
                .id(todo.getId())
                .name(todo.getName())
                .priority(todo.getPriority())
                .description(todo.getDescription())
                .username(FakeUsers.LOGGED_USER_PLAIN.getLogin())
                .completed(todo.isCompleted())
                .build();
    }

    private Todo withCreatedId(Todo todo) {
        return Todo.builder()
                .id(ID)
                .name(todo.getName())
                .priority(todo.getPriority())
                .description(todo.getDescription())
                .username(todo.getUsername())
                .completed(todo.isCompleted())
                .build();
    }

    @Test
    public void get_todoNotFoundInRepo_throwsEntityNotFoundException() {
        // given
        when(fakeRepository.findOne(ID)).thenReturn(null);
        // when
        try {
            todoService.get(ID);
            fail();
        } catch (EntityNotFoundException e) {
            // then pass
        }
        verify(fakeRepository).findOne(ID);
    }

    private void assertSameTodoButWithoutUsername(Todo todo) {
        assertThat(todo.getId()).isEqualTo(PLAIN_USER_TODO.getId());
        assertThat(todo.getName()).isEqualTo(PLAIN_USER_TODO.getName());
        assertThat(todo.getPriority()).isEqualTo(PLAIN_USER_TODO.getPriority());
        assertThat(todo.getDescription()).isEqualTo(PLAIN_USER_TODO.getDescription());
        assertThat(todo.isCompleted()).isEqualTo(PLAIN_USER_TODO.isCompleted());
        assertThat(todo.getUsername()).isEqualTo(null);
    }

    @Test
    public void update_todoNotFoundInRepo_addsGivenTodoToRepo() {
        // given
        when(fakeRepository.findOne(ID)).thenReturn(null);
        when(currentUserProvider.getCurrentUserName()).thenReturn("user");
        when(fakeRepository.save(eq(expectedToSave()))).thenReturn(expectedToSave());
        // when
        todoService.update(PLAIN_USER_TODO);
        // then
        verify(fakeRepository).findOne(ID);
        verify(currentUserProvider).getCurrentUserName();
        verify(fakeRepository).save(expectedToSave());
    }

    private Todo expectedToSave() {
        return Todo.builder()
                .id(ID)
                .name(PLAIN_USER_TODO.getName())
                .priority(PLAIN_USER_TODO.getPriority())
                .description(PLAIN_USER_TODO.getDescription())
                .username("user")
                .completed(PLAIN_USER_TODO.isCompleted())
                .build();
    }

    private Todo withUsernameFromRepo(Todo todo) {
        return Todo.builder()
                .id(todo.getId())
                .name(todo.getName())
                .priority(todo.getPriority())
                .description(todo.getDescription())
                .username(PLAIN_USER_TODO.getUsername())
                .completed(todo.isCompleted())
                .build();
    }

    @Test
    public void update_userIsLoggedInAndWannaUpdateOthersTodo__itIsForbidden() throws Exception {
        // given
        plainUserIsLoggedIn();
        when(fakeRepository.findOne(ID)).thenReturn(ADMIN_USER_TODO);
        // when
        try {
            todoService.update(ADMIN_USER_TODO);
            fail();
        } catch (ForbiddenException e) {
            // then pass
        }
    }

    @Test
    public void delete_todoNotFoundInRepo_throwsEntityNotFoundException() {
        // given
        when(fakeRepository.findOne(ID)).thenReturn(null);
        // when
        try {
            todoService.delete(ID);
            fail();
        } catch (EntityNotFoundException e) {
            // then pass
        }
        verify(fakeRepository).findOne(ID);
        verify(fakeRepository, times(0)).delete(any(Long.class));
    }

    @Test
    public void delete_userIsLoggedInAndWannaDeleteOthersTodo__itIsForbidden() throws Exception {
        // given
        plainUserIsLoggedIn();
        when(fakeRepository.findOne(ID)).thenReturn(ADMIN_USER_TODO);
        // when
        try {
            todoService.delete(ID);
            fail();
        } catch (ForbiddenException e) {
            // then pass
        }
        verify(fakeRepository, times(0)).delete(any(Long.class));
    }

}