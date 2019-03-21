package com.training.todo.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.training.security.service.CurrentUserProvider;
import com.training.todo.controller.dto.TodoSearchParamsDto;
import com.training.todo.domain.Todo;
import com.training.todo.persistance.TodoJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class SearchTodoService {

    private TodoJpaRepository todoRepository;

    private CurrentUserProvider currentUserProvider;

    @Autowired
    public SearchTodoService(TodoJpaRepository todoRepository, CurrentUserProvider currentUserProvider) {
        this.todoRepository = todoRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @PreAuthorize("isAuthenticated()")
    public Page<Todo> find(TodoSearchParamsDto searchParams, Pageable pageable) {
        if (currentUserProvider.isAdminLoggedIn()) {
            return findForAdmin(searchParams, pageable);
        } else {
            return findForUser(searchParams, pageable);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    private Page<Todo> findForAdmin(TodoSearchParamsDto searchParams, Pageable pageable){
        boolean nameIsPresent = !Strings.isNullOrEmpty(searchParams.getName());
        boolean priorityIsPresent = !Strings.isNullOrEmpty(searchParams.getPriority());
        if(nameIsPresent && priorityIsPresent){
            return todoRepository.findByNameAndPriority(searchParams.getName(), searchParams.getPriority(), pageable);
        }
        if(nameIsPresent){
            return todoRepository.findByName(searchParams.getName(), pageable);
        }
        if(priorityIsPresent){
            return todoRepository.findByPriority(searchParams.getPriority(), pageable);
        }
        return todoRepository.findAll(pageable);
    }

    private Page<Todo> findForUser(TodoSearchParamsDto searchParams, Pageable pageable) {
        boolean nameIsPresent = !Strings.isNullOrEmpty(searchParams.getName());
        boolean priorityIsPresent = !Strings.isNullOrEmpty(searchParams.getPriority());
        Page<Todo> todos;
        if (nameIsPresent && priorityIsPresent){
            todos = findByNameAndPriorityForUser(searchParams, pageable);
        } else if(nameIsPresent) {
            todos = findByNameForUser(searchParams, pageable);

        } else if(priorityIsPresent){
            todos = findByPriorityForUser(searchParams, pageable);

        } else {
            todos = findAllForUser(pageable);
        }
        return todos;
    }

    private Page<Todo> findByNameAndPriorityForUser(TodoSearchParamsDto searchParams, Pageable pageable) {
        return todoRepository.findByNameAndPriorityAndUsername(
                searchParams.getName(),
                searchParams.getPriority(),
                currentUserProvider.getCurrentUserName(),
                pageable);
    }

    private Page<Todo> findByNameForUser(TodoSearchParamsDto searchParams, Pageable pageable) {
        return todoRepository.findByNameAndUsername(
                searchParams.getName(), currentUserProvider.getCurrentUserName(), pageable);
    }

    private Page<Todo> findByPriorityForUser(TodoSearchParamsDto searchParams, Pageable pageable) {
        return todoRepository.findByPriorityAndUsername(
                searchParams.getPriority(), currentUserProvider.getCurrentUserName(), pageable);
    }

    private Page<Todo> findAllForUser(Pageable pageable) {
        return todoRepository.findByUsername(currentUserProvider.getCurrentUserName(), pageable);
    }
}