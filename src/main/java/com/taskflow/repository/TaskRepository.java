package com.taskflow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskflow.entity.TaskEntity;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    List<TaskEntity> findByProjectId(Long projectId);

    List<TaskEntity> findByProjectIdAndStatus(Long projectId, TaskStatus status);

    List<TaskEntity> findByProjectIdAndPriority(Long projectId, TaskPriority priority);

    List<TaskEntity> findByProjectIdAndStatusAndPriority(
            Long projectId,
            TaskStatus status,
            TaskPriority priority
    );
}
