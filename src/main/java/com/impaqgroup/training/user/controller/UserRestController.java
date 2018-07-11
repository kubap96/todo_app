package com.impaqgroup.training.user.controller;

import com.impaqgroup.training.user.controller.dto.UserDto;
import com.impaqgroup.training.user.domain.User;
import com.impaqgroup.training.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserRestController {

    private UserService service;

    public UserRestController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return service.getAllUsers(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid User user) {
        return service.create(user);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{login}")
    public void delete(@PathVariable("login") String login) {
        service.delete(login);
    }

    @PostMapping("/{login}/reset-password")
    public String resetPassword(@PathVariable("login") String login) {
        return service.resetPassword(login);
    }

    @PatchMapping("/{login}/role")
    public void updateRole(@PathVariable("login") String login,
                           @RequestBody User.Role role) {
        service.updateRole(login, role);
    }

    @PatchMapping("/{login}/password")
    public void updatePassword(@PathVariable("login") String login,
                               @RequestBody String password) {
        service.updatePassword(login, password);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String notFoundHandler(){
        return "nie znaleziono elementu o podanym loginie";
    }
}
