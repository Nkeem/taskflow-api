package com.taskflow.kafka.event;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.taskflow.enums.TaskStatus;

public record TaskStatusChangedEvent(
        Long taskId,
        Long projectId,
        String eventType,
        TaskStatus oldStatus,
        TaskStatus newStatus,
        LocalDateTime createdAt
) implements Serializable {
}
