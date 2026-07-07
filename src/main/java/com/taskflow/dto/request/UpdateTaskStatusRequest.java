package com.taskflow.dto.request;

import com.taskflow.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(
        @NotNull
        TaskStatus status
) {
}
