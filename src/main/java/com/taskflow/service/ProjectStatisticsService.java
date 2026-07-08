package com.taskflow.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskflow.dto.response.ProjectStatisticsResponse;
import com.taskflow.entity.TaskEntity;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectStatisticsService {

    private final ProjectService projectService;
    private final TaskRepository taskRepository;

    @Cacheable(value = "projectStatistics", key = "#projectId")
    public ProjectStatisticsResponse getProjectStatistics(Long projectId) {
        log.info("Calculating statistics for project id: {}", projectId);
        projectService.getProjectEntityById(projectId);

        List<TaskEntity> tasks = taskRepository.findByProjectId(projectId);
        LocalDateTime now = LocalDateTime.now();

        long totalTasks = tasks.size();
        long todo = countByStatus(tasks, TaskStatus.TODO);
        long inProgress = countByStatus(tasks, TaskStatus.IN_PROGRESS);
        long done = countByStatus(tasks, TaskStatus.DONE);
        long highPriority = countByPriority(tasks, TaskPriority.HIGH);
        long overdue = countOverdue(tasks, now);

        return new ProjectStatisticsResponse(
                projectId,
                totalTasks,
                todo,
                inProgress,
                done,
                highPriority,
                overdue
        );
    }

    private long countByStatus(List<TaskEntity> tasks, TaskStatus status) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .count();
    }

    private long countByPriority(List<TaskEntity> tasks, TaskPriority priority) {
        return tasks.stream()
                .filter(task -> task.getPriority() == priority)
                .count();
    }

    private long countOverdue(List<TaskEntity> tasks, LocalDateTime now) {
        return tasks.stream()
                .filter(task -> task.getDeadline() != null)
                .filter(task -> task.getDeadline().isBefore(now))
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .count();
    }
}
