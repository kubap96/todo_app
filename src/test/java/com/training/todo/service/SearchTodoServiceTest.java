package com.training.todo.service;


import com.training.security.service.CurrentUserProvider;
import com.training.todo.controller.dto.TodoSearchParamsDto;
import com.training.todo.domain.Todo;
import com.training.todo.persistance.TodoJpaRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static com.training.utils.FakePageable.FAKE_PAGEABLE;
import static com.training.utils.FakeUsers.ADMIN_USER;
import static com.training.utils.FakeUsers.PLAIN_USER;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SearchTodoServiceTest {

    private static final String PRIORITY = "priorytet";
    private static final String NAME = "nazwa";

    private static final Todo UNIQUE_NAME = new Todo(
            1L, "unikalna nazwa", PRIORITY, "opis", true, PLAIN_USER.getLogin());

    private static final Todo UNIQUE_PRIORITY = new Todo(
            1L, NAME, "unikalny priorytet", "opis", true, PLAIN_USER.getLogin());

    private static final Todo UNIQUE_NAME_AND_PRIORITY = new Todo(
            1L, "unikalna nazwa i priorytet", "unikalna nazwa i priorytet", "opis", true, ADMIN_USER.getLogin());

    private static final Todo NOT_UNIQUE = new Todo(
            1L, NAME, PRIORITY, "opis", true, ADMIN_USER.getLogin());

    private static final Todo ANOTHER_NOT_UNIQUE = new Todo(
            1L, NAME, PRIORITY, "opis", true, ADMIN_USER.getLogin());

    private static final Todo PLAIN_USER_TODO_WITH_UNIQUE_NAME = UNIQUE_NAME;
    private static final Todo PLAIN_USER_TODO_WITH_UNIQUE_PRIORITY = UNIQUE_PRIORITY;

    @Mock
    private TodoJpaRepository fakeRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private SearchTodoService searchService;

    private final ArrayList<Todo> todos = todos();

    private ArrayList<Todo> todos() {
        ArrayList<Todo> fakeTodos = new ArrayList<>();
        fakeTodos.add(NOT_UNIQUE);
        fakeTodos.add(ANOTHER_NOT_UNIQUE);
        fakeTodos.add(UNIQUE_NAME);
        fakeTodos.add(UNIQUE_PRIORITY);
        fakeTodos.add(UNIQUE_NAME_AND_PRIORITY);
        return fakeTodos;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void findForAdmin_searchByNameOnly_returnsElementFoundInRepo() {
        //given
        Todo todo = new Todo(1L, "nazwa", "priorytet", "opis", true, null);
        TodoSearchParamsDto searchParams = new TodoSearchParamsDto("nazwa", null);
        whenAdminIsLoggedIn();
        when(fakeRepository.findByName(eq(searchParams.getName()), eq(FAKE_PAGEABLE)))
                .thenReturn(newPage(todo));
        //when
        Page<Todo> todos = searchService.find(searchParams, FAKE_PAGEABLE);
        //then
        assertThat(todos).hasSize(1);
        assertThat(todos).contains(todo);
        assertThat(todos.getContent().get(0)).isEqualTo(todo);
        verify(fakeRepository, times(1))
                .findByName(eq(searchParams.getName()), eq(FAKE_PAGEABLE));
        verifyNoMoreInteractions(fakeRepository);
    }

    private void whenAdminIsLoggedIn() {
        when(currentUserProvider.isAdminLoggedIn()).thenReturn(true);
    }

    private Page<Todo> newPage(Todo... todos) {
        return new PageImpl<>(newArrayList(todos));
    }

    @Test
    public void findForAdmin_searchByNameOnlyWhenThereIsNoMatchInRepo_returnsEmptyList() {
        //given
        TodoSearchParamsDto searchParams = new TodoSearchParamsDto(
                "nazwa1234", null);
        whenAdminIsLoggedIn();
        when(fakeRepository.findByName(eq(searchParams.getName()), eq(FAKE_PAGEABLE)))
                .thenReturn(new PageImpl<>(emptyList()));
        //when
        Page<Todo> actualTodos = searchService.find(searchParams, FAKE_PAGEABLE);
        //then
        assertThat(actualTodos).isEmpty();
        verify(fakeRepository, times(1))
                .findByName(eq(searchParams.getName()), eq(FAKE_PAGEABLE));
    }

    @Test
    public void findForAdmin_searchByNameOnly_returnsManyElementsFoundInRepo() {
        //given
        TodoSearchParamsDto searchParams = new TodoSearchParamsDto(
                NAME, null);
        whenAdminIsLoggedIn();
        when(fakeRepository.findByName(eq(searchParams.getName()), eq(FAKE_PAGEABLE)))
                .thenReturn(newPage(UNIQUE_PRIORITY, NOT_UNIQUE, ANOTHER_NOT_UNIQUE));
        //when
        Page<Todo> actualTodos = searchService.find(searchParams, FAKE_PAGEABLE);
        //then
        assertThat(actualTodos.getContent().size()).isEqualTo(3);
        assertThat(actualTodos).containsOnly(UNIQUE_PRIORITY, NOT_UNIQUE, ANOTHER_NOT_UNIQUE);
        verify(fakeRepository, times(1))
                .findByName(eq(searchParams.getName()), eq(FAKE_PAGEABLE));
    }

    @Test
    public void findForAdmin_searchByPriorityOnly_returnsElementFoundInRepo() {
        //given
        TodoSearchParamsDto searchParams = new TodoSearchParamsDto(
                null, UNIQUE_PRIORITY.getPriority());
        whenAdminIsLoggedIn();
        when(fakeRepository.findByPriority(eq(searchParams.getPriority()), eq(FAKE_PAGEABLE)))
                .thenReturn(newPage(UNIQUE_PRIORITY));
        //when
        Page<Todo> actualTodos = searchService.find(searchParams, FAKE_PAGEABLE);
        //then
        assertThat(actualTodos).hasSize(1);
        assertThat(actualTodos).containsOnly(UNIQUE_PRIORITY);
        verify(fakeRepository, times(1))
                .findByPriority(eq(searchParams.getPriority()), eq(FAKE_PAGEABLE));
    }

    @Test
    public void findForAdmin_searchByPriorityOnlyWhenThereIsNoMatchInRepo_returnsEmptyList() {
        //given
        TodoSearchParamsDto searchParams = new TodoSearchParamsDto(
                null, "priorytet1234");
        whenAdminIsLoggedIn();
        when(fakeRepository.findByPriority(eq(searchParams.getPriority()), eq(FAKE_PAGEABLE)))
                .thenReturn(new PageImpl<>(emptyList()));
        //when
        Page<Todo> actualTodos = searchService.find(searchParams, FAKE_PAGEABLE);
        //then
        assertThat(actualTodos).isEmpty();
        verify(fakeRepository, times(1))
                .findByPriority(eq(searchParams.getPriority()), eq(FAKE_PAGEABLE));
    }

    @Test
    public void findForAdmin_searchByPriorityOnly_returnsManyElementsFoundInRepo() {
        //given
        TodoSearchParamsDto searchParams = new TodoSearchParamsDto(
                "", PRIORITY);
        whenAdminIsLoggedIn();
        when(fakeRepository.findByPriority(eq(searchParams.getPriority()), eq(FAKE_PAGEABLE)))
                .thenReturn(newPage(UNIQUE_NAME, NOT_UNIQUE, ANOTHER_NOT_UNIQUE));
        //when
        Page<Todo> actualTodos = searchService.find(searchParams, FAKE_PAGEABLE);
        //then
        assertThat(actualTodos).hasSize(3);
        assertThat(actualTodos).containsOnly(UNIQUE_NAME, NOT_UNIQUE, ANOTHER_NOT_UNIQUE);
        verify(fakeRepository, times(1))
                .findByPriority(eq(searchParams.getPriority()), eq(FAKE_PAGEABLE));
    }

    @Test
    public void findForAdmin_searchByNameAndPriority_returnsElementFoundInRepo() {
        //given
        TodoSearchParamsDto searchParams = new TodoSearchParamsDto(
                UNIQUE_NAME_AND_PRIORITY.getName(),
                UNIQUE_NAME_AND_PRIORITY.getPriority());
        whenAdminIsLoggedIn();
        when(fakeRepository.findByNameAndPriority(
                searchParams.getName(), searchParams.getPriority(), FAKE_PAGEABLE))
                .thenReturn(newPage(UNIQUE_NAME_AND_PRIORITY));
        //when
        Page<Todo> actualTodos = searchService.find(searchParams, FAKE_PAGEABLE);
        //then
        assertThat(actualTodos).hasSize(1);
        assertThat(actualTodos).containsOnly(UNIQUE_NAME_AND_PRIORITY);
        verify(fakeRepository, times(1))
                .findByNameAndPriority(
                        eq(searchParams.getName()),
                        eq(searchParams.getPriority()),
                        eq(FAKE_PAGEABLE));
    }

    @Test
    public void findForAdmin_searchByNameAndPriority_returnsManyElementsFoundInRepo() {
        //given
        TodoSearchParamsDto searchParams = new TodoSearchParamsDto(
                NAME, PRIORITY);
        whenAdminIsLoggedIn();
        when(fakeRepository.findByNameAndPriority(
                eq(searchParams.getName()), eq(searchParams.getPriority()), eq(FAKE_PAGEABLE)))
                .thenReturn(newPage(NOT_UNIQUE, ANOTHER_NOT_UNIQUE));
        //when
        Page<Todo> actualTodos = searchService.find(searchParams, FAKE_PAGEABLE);
        //then
        assertThat(actualTodos).hasSize(2);
        assertThat(actualTodos).containsOnly(ANOTHER_NOT_UNIQUE, NOT_UNIQUE);
        verify(fakeRepository, times(1))
                .findByNameAndPriority(
                        eq(searchParams.getName()),
                        eq(searchParams.getPriority()),
                        eq(FAKE_PAGEABLE));
    }

    @Test
    public void findForAdmin_searchByNameAndPriorityOnlyWhenThereIsNoMatchInRepo_returnsEmptyList() {
        //given
        TodoSearchParamsDto searchParams = new TodoSearchParamsDto(
                "nazwa1234", "priorytet1234");
        whenAdminIsLoggedIn();
        when(fakeRepository.findByNameAndPriority(
                eq(searchParams.getName()), eq(searchParams.getPriority()), eq(FAKE_PAGEABLE)))
                .thenReturn(new PageImpl<>(emptyList()));
        //when
        Page<Todo> actualTodos = searchService.find(searchParams, FAKE_PAGEABLE);
        //then
        assertThat(actualTodos).isEmpty();
        verify(fakeRepository, times(1))
                .findByNameAndPriority(
                        eq(searchParams.getName()),
                        eq(searchParams.getPriority()),
                        eq(FAKE_PAGEABLE));
    }

    @Test
    public void findForAdmin_searchWithoutParameters_returnsAllElementsFromRepo() {
        //given
        TodoSearchParamsDto searchParams = new TodoSearchParamsDto(null, "");
        whenAdminIsLoggedIn();
        when(fakeRepository.findAll(eq(FAKE_PAGEABLE)))
                .thenReturn(new PageImpl<>(todos));
        //when
        Page<Todo> actualTodos = searchService.find(searchParams, FAKE_PAGEABLE);
        //then
        assertThat(actualTodos).hasSize(todos.size());
        assertThat(actualTodos).containsOnly(
                NOT_UNIQUE,
                ANOTHER_NOT_UNIQUE,
                UNIQUE_NAME,
                UNIQUE_PRIORITY,
                UNIQUE_NAME_AND_PRIORITY);
        verify(fakeRepository, times(1)).findAll(eq(FAKE_PAGEABLE));
    }

    private void whenPlainUserIsLoggedInThenReturnHisLogin() {
        when(currentUserProvider.isAdminLoggedIn()).thenReturn(false);
        when(currentUserProvider.getCurrentUserName()).thenReturn(PLAIN_USER.getLogin());
    }
}