package com.taskflow.dto.request;

import java.time.LocalDateTime;

import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(
        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description,

        @NotNull
        TaskStatus status,

        @NotNull
        TaskPriority priority,

        LocalDateTime deadline,

        Long assigneeId
) {
}
