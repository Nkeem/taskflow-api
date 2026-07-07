package com.taskflow.dto.response;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        UserResponse owner,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
