package com.training.todo.service;

import com.training.security.service.CurrentUserProvider;
import com.training.todo.domain.Todo;
import com.training.todo.persistance.TodoJpaRepository;
import com.training.todo.utils.ForbiddenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
public class TodoService {

    private TodoJpaRepository todoRepository;

    private CurrentUserProvider currentUserProvider;

    @Autowired
    public TodoService(TodoJpaRepository todoRepository, CurrentUserProvider currentUserProvider) {
        this.todoRepository = todoRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @PreAuthorize("isAuthenticated()")
    public Page<Todo> getAll(Pageable pageable){
        if (currentUserProvider.isAdminLoggedIn()) {
            return getAllForAdmin(pageable);
        } else {
            return getAllForUser(pageable);
        }
    }

    private Page<Todo> getAllForAdmin(Pageable pageable) {
        return todoRepository.findAll(pageable);
    }

    private Page<Todo> getAllForUser(Pageable pageable) {
        return todoRepository.findByUsername(
                currentUserProvider.getCurrentUserName(), pageable);
    }

    @PreAuthorize("isAuthenticated()")
    public Long create(Todo todo) {
        todo.setUsername(currentUserProvider.getCurrentUserName());
        return todoRepository.save(todo).getId();
    }

    @PreAuthorize("isAuthenticated()")
    public Todo get(Long id) {
        Todo result = todoRepository.findOne(id);
        if(result == null){
            throw new EntityNotFoundException();
        }
        if (hasAccessTo(result)) {
            return result;
        }
        throw new ForbiddenException("User " + currentUserProvider.getCurrentUserName() +
                " tried to get todo which not belong to him");
    }

    @PreAuthorize("isAuthenticated()")
    public void update(Todo todo) {
        Optional<Todo> fromDatabase = ofNullable(todoRepository.findOne(todo.getId()));
        if (isNotPresent(fromDatabase)) {
            create(todo);
            return;
        }
        if (hasAccessTo(fromDatabase.get())) {
            todo.setUsername(fromDatabase.get().getUsername());
            todoRepository.save(todo);
            return;
        }
        throw new ForbiddenException("User " + currentUserProvider.getCurrentUserName() +
                " tried to update todo which not belong to him");
    }

    @PreAuthorize("isAuthenticated()")
    public void delete(Long id) {
        Optional<Todo> todo = ofNullable(todoRepository.findOne(id));
        if (isNotPresent(todo)) {
            throw new EntityNotFoundException();
        }
        if (hasAccessTo(todo.get())) {
            todoRepository.delete(id);
            return;
        }
        throw new ForbiddenException("User " + currentUserProvider.getCurrentUserName() +
                " tried to delete todo which not belong to him");
    }

    private boolean isNotPresent(Optional optional) {
        return !optional.isPresent();
    }

    boolean hasAccessTo(Todo todo) {
        return currentUserProvider.isAdminLoggedIn() || belongsToCurrentUser(todo);
    }

    private boolean belongsToCurrentUser(Todo todo) {
        return currentUserProvider.getCurrentUserName()
                .equals(todo.getUsername());
    }

}