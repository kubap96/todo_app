package com.impaqgroup.training.user.service;

import com.impaqgroup.training.utils.FakeUsers;
import com.impaqgroup.training.security.service.CurrentUserProvider;
import com.impaqgroup.training.user.controller.dto.UserDto;
import com.impaqgroup.training.user.domain.User;
import com.impaqgroup.training.user.persistance.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.impaqgroup.training.utils.FakePageable.FAKE_PAGEABLE;
import static com.impaqgroup.training.utils.FakeUsers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private static final String PASSWORD_HASH = "1a1a1a1a1a1a";

    private static final ArgumentCaptor<User> USER_ARGUMENT = ArgumentCaptor.forClass(User.class);

    @Mock
    private UserRepository fakeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private UserService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(passwordEncoder.encode(any())).thenReturn(PASSWORD_HASH);
    }

    @Test
    public void getAllUsers_usersFound_returnsSameUsersButWithoutPassword() {
        // given
        when( fakeRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(ALL_USERS));
        // when
        Page<UserDto> result = service.getAllUsers(FAKE_PAGEABLE);
        List<String> actualLogins = extractLogins(result.getContent());
        // then
        assertThat(result).hasSize(ALL_USERS.size());
        assertThat(actualLogins).containsOnly(FakeUsers.PLAIN_USER.getLogin(), ADMIN_USER.getLogin());
        verify(fakeRepository, times(1)).findAll(any(Pageable.class));
    }

    private List<String> extractLogins(List<UserDto> result) {
        return result.stream()
                .map(UserDto::getLogin)
                .collect(Collectors.toList());
    }

    @Test
    public void getAllUsers_userFound_returnsUserWithSamePropertiesExceptPassword() {
        // given
        when( fakeRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(newArrayList(FakeUsers.PLAIN_USER)));
        // when
        UserDto actual = service.getAllUsers(FAKE_PAGEABLE)
                .getContent().get(0);
        // then
        assertThat(actual.getLogin()).isEqualTo(FakeUsers.PLAIN_USER.getLogin());
        assertThat(actual.getRole()).isEqualTo(FakeUsers.PLAIN_USER.getRole());
        verify(fakeRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    public void create_userNotExist_createsGivenUserAndReturnsDto() {
        // given
        when(fakeRepository.exists(PLAIN_USER.getLogin())).thenReturn(false);
        when(fakeRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);
        // when
        UserDto userDto = service.create(PLAIN_USER);
        // then
        assertThat(userDto.getLogin()).isEqualTo(PLAIN_USER.getLogin());
        assertThat(userDto.getRole()).isEqualTo(PLAIN_USER.getRole());
        verify(fakeRepository).exists(PLAIN_USER.getLogin());
    }

    @Test
    public void create_userNotExist_encryptUserPasswordBeforeSave() {
        // given
        when(fakeRepository.exists(PLAIN_USER.getLogin())).thenReturn(false);
        when(fakeRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);
        // when
        service.create(PLAIN_USER);
        // then
        verify(fakeRepository).save(USER_ARGUMENT.capture());
        User actual = USER_ARGUMENT.getValue();
        assertThat(actual.getLogin()).isEqualTo(PLAIN_USER.getLogin());
        assertThat(actual.getPasswordHash()).isEqualTo(PASSWORD_HASH);
    }

    @Test
    public void create_userAlreadyExists_doesNotAllowToOverwriteExistingUser() {
        // given
        when(fakeRepository.exists(PLAIN_USER.getLogin())).thenReturn(true);
        // when
        try {
            service.create(PLAIN_USER);
            fail("Create should not allow to overwrite existing user");
        } catch (EntityExistsException e) {
            // then pass
        }
    }

    @Test
    public void delete_userWithGivenLoginNotFoundInRepo_throwsEntityNotFoundException() {
        // given
        adminIsLoggedIn();
        doThrow(EmptyResultDataAccessException.class)
                .when(fakeRepository).delete("login");
        // when
        try {
            service.delete("login");
            fail("Should throw EntityNotFoundException when user " +
                    "with given login not found in repo!");
        } catch (EntityNotFoundException e) {
            // then pass
        }
    }

    private void adminIsLoggedIn() {
        when(currentUserProvider.getCurrentUserName())
                .thenReturn(LOGGED_USER_ADMIN.getLogin());
    }

    @Test
    public void delete_givenLoginBelongsToOtherUser_deletesUser() {
        // given
        adminIsLoggedIn();
        // when
        service.delete(LOGGED_USER_PLAIN.getLogin());
        // then
        verify(fakeRepository).delete(LOGGED_USER_PLAIN.getLogin());
    }

    @Test
    public void resetPassword_loginNotExist_throwsEntityNotFoundException() {
        // given
        when(fakeRepository.exists(PLAIN_USER.getLogin())).thenReturn(false);
        // when
        try {
            service.resetPassword(PLAIN_USER.getLogin());
            fail("Should throw EntityNotFoundException when login not exist");
        } catch (EntityNotFoundException e) {
            // then pass
        }
    }

    @Test
    public void resetPassword_loginExists_returnsGeneratedPassword() {
        // given
        when(fakeRepository.exists(PLAIN_USER.getLogin())).thenReturn(true);
        // when
        String generatedPassword = service.resetPassword(PLAIN_USER.getLogin());
        // then
        assertThat(generatedPassword).isNotEmpty();
        assertThat(generatedPassword).isNotEqualTo(PLAIN_USER.getPasswordHash());
        verify(fakeRepository).exists(PLAIN_USER.getLogin());
    }

    @Test
    public void resetPassword_loginExists_generatesRandomPassword() {
        // given
        when(fakeRepository.exists(PLAIN_USER.getLogin())).thenReturn(true);
        // when
        String password = service.resetPassword(PLAIN_USER.getLogin());
        String password2 = service.resetPassword(PLAIN_USER.getLogin());
        // then
        assertThat(password).isNotEqualTo(password2);
    }

    @Test
    public void resetPassword_loginExists_updatesUserWithEncryptedPassword() {
        // given
        when(fakeRepository.exists(PLAIN_USER.getLogin())).thenReturn(true);
        when(passwordEncoder.encode(any())).thenReturn(PASSWORD_HASH);
        // when
        String generatedPassword = service.resetPassword(PLAIN_USER.getLogin());
        // then
        verify(passwordEncoder).encode(generatedPassword);
        verify(fakeRepository)
                .updatePassword(PLAIN_USER.getLogin(), PASSWORD_HASH);
    }

    @Test
    public void updateRole_loginNotExist_throwsEntityNotFoundException() {
        // given
        when(fakeRepository.exists(PLAIN_USER.getLogin())).thenReturn(false);
        // when
        try {
            service.updateRole(PLAIN_USER.getLogin(), User.Role.ADMIN);
            fail("Should throw EntityNotFoundException when login not exist");
        } catch (EntityNotFoundException e) {
            // then pass
        }
        verify(fakeRepository).exists(PLAIN_USER.getLogin());
    }

    @Test
    public void updateRole_loginExists_updatesUserRole() {
        // given
        adminIsLoggedIn();
        when(fakeRepository.exists(PLAIN_USER.getLogin())).thenReturn(true);
        // when
        service.updateRole(
                PLAIN_USER.getLogin(), User.Role.ADMIN);
        // then
        verify(fakeRepository)
                .updateRole(PLAIN_USER.getLogin(), User.Role.ADMIN);
    }

    @Test
    public void updatePassword_loginNotExist_throwsEntityNotFoundException() {
        // given
        when(fakeRepository.exists(PLAIN_USER.getLogin())).thenReturn(false);
        // when
        try {
            service.updatePassword(PLAIN_USER.getLogin(), PLAIN_USER.getPasswordHash());
            fail("Should throw EntityNotFoundException when login not exist");
        } catch (EntityNotFoundException e) {
            // then pass
        }
        verify(fakeRepository).exists(PLAIN_USER.getLogin());
    }

    private void plainUserIsLoggedIn() {
        when(currentUserProvider.getCurrentUserName()).thenReturn(PLAIN_USER.getLogin());
    }

    @Test
    public void updatePassword_userWannaUpdateOwnPassword_updatesWithEnctyptedPassword() {
        // given
        when(fakeRepository.exists(PLAIN_USER.getLogin())).thenReturn(true);
        plainUserIsLoggedIn();
        String password = "new password";
        when(passwordEncoder.encode(password)).thenReturn(PASSWORD_HASH);
        // when
        service.updatePassword(PLAIN_USER.getLogin(), password);
        // then
        verify(fakeRepository)
                .updatePassword(PLAIN_USER.getLogin(), PASSWORD_HASH);
    }
}