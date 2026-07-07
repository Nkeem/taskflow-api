package com.taskflow.dto.response;

import java.time.LocalDateTime;

import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime deadline,
        Long projectId,
        UserResponse assignee,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
