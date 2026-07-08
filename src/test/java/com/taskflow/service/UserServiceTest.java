package com.taskflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskflow.dto.request.CreateUserRequest;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.UserEntity;
import com.taskflow.exception.BaseException;
import com.taskflow.exception.BusinessError;
import com.taskflow.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_shouldCreateUser_whenEmailAndUsernameAreUnique() {
        CreateUserRequest request = new CreateUserRequest("john", "john@example.com");
        UserEntity savedUser = createUser(1L, "john", "john@example.com");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("john");
        assertThat(response.email()).isEqualTo("john@example.com");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("john", "john@example.com");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getError()).isEqualTo(BusinessError.USER_ALREADY_EXISTS)
                );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void createUser_shouldThrowException_whenUsernameAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("john", "john@example.com");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByUsername(request.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getError()).isEqualTo(BusinessError.USER_ALREADY_EXISTS)
                );

        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {
        UserEntity user = createUser(1L, "john", "john@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("john");
        assertThat(response.email()).isEqualTo("john@example.com");
    }

    @Test
    void getUserById_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1L))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getError()).isEqualTo(BusinessError.USER_NOT_FOUND)
                );
    }

    @Test
    void getUserEntityById_shouldReturnEntity_whenUserExists() {
        UserEntity user = createUser(1L, "john", "john@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserEntity response = userService.getUserEntityById(1L);

        assertThat(response).isSameAs(user);
    }

    private UserEntity createUser(Long id, String username, String email) {
        return UserEntity.builder()
                .id(id)
                .username(username)
                .email(email)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
