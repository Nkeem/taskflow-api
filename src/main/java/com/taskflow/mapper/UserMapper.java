package com.taskflow.mapper;

import com.taskflow.dto.response.UserResponse;
import com.taskflow.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(UserEntity user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
