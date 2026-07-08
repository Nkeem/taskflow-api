package com.taskflow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskflow.dto.request.CreateUserRequest;
import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.UserEntity;
import com.taskflow.exception.BaseException;
import com.taskflow.exception.BusinessError;
import com.taskflow.mapper.UserMapper;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BaseException(
                    BusinessError.USER_ALREADY_EXISTS,
                    "User with email already exists"
            );
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new BaseException(
                    BusinessError.USER_ALREADY_EXISTS,
                    "User with username already exists"
            );
        }

        UserEntity user = UserEntity.builder()
                .username(request.username())
                .email(request.email())
                .build();

        UserEntity savedUser = userRepository.save(user);
        return UserMapper.toResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        return UserMapper.toResponse(getUserEntityById(id));
    }

    public UserEntity getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BaseException(
                        BusinessError.USER_NOT_FOUND,
                        "User with id " + id + " not found"
                ));
    }
}
