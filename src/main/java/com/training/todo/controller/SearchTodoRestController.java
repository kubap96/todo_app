package com.training.todo.controller;

import com.training.todo.controller.dto.TodoSearchParamsDto;
import com.training.todo.domain.Todo;
import com.training.todo.service.SearchTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search/todos")
public class SearchTodoRestController {

    private SearchTodoService searchService;

    @Autowired
    public SearchTodoRestController(SearchTodoService searchService) {
        this.searchService = searchService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Page<Todo> getAll(@RequestParam(value="name", required = false) String name,
                             @RequestParam(value="priority", required = false) String priority,
                             Pageable pageRequest){
        return searchService.find(new TodoSearchParamsDto(name, priority), pageRequest);
    }
}
