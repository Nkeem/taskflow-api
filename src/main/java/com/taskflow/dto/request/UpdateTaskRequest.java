package com.taskflow.dto.request;

import java.time.LocalDateTime;

import com.taskflow.enums.TaskPriority;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description,

        TaskPriority priority,

        LocalDateTime deadline,

        Long assigneeId
) {
}
