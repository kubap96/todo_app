package com.training.todo.controller;


import com.training.todo.domain.Todo;
import com.training.todo.service.TodoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/todos")
public class TodoRestController {

    private TodoService todoService;

    public TodoRestController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public Page<Todo> getAll(Pageable pageRequest){
        return todoService.getAll(pageRequest);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody @Valid Todo todo){
        todo.setId(null);
        return todoService.create(todo);
    }

    @GetMapping("/{id}")
    public Todo get(@PathVariable("id") Long id){
        return todoService.get(id);
    }

    @PutMapping("/{id}")
    public void update(@RequestBody @Valid Todo todo, @PathVariable("id") Long id){
        todo.setId(id);
        todoService.update(todo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id){
        todoService.delete(id);
    }
}