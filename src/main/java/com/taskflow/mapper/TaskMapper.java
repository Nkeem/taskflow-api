package com.taskflow.mapper;

import com.taskflow.dto.response.TaskResponse;
import com.taskflow.entity.TaskEntity;

public final class TaskMapper {

    private TaskMapper() {
    }

    public static TaskResponse toResponse(TaskEntity task) {
        if (task == null) {
            return null;
        }

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDeadline(),
                task.getProject().getId(),
                UserMapper.toResponse(task.getAssignee()),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
