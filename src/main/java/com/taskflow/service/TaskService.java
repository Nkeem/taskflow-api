package com.taskflow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskflow.dto.request.CreateTaskRequest;
import com.taskflow.dto.request.UpdateTaskRequest;
import com.taskflow.dto.request.UpdateTaskStatusRequest;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.entity.ProjectEntity;
import com.taskflow.entity.TaskEntity;
import com.taskflow.entity.UserEntity;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.exception.BaseException;
import com.taskflow.exception.BusinessError;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectStatisticsCacheService projectStatisticsCacheService;

    @Transactional
    public TaskResponse createTask(Long projectId, CreateTaskRequest request) {
        ProjectEntity project = projectService.getProjectEntityById(projectId);
        UserEntity assignee = getAssignee(request.assigneeId());

        TaskEntity task = TaskEntity.builder()
                .title(request.title())
                .description(request.description())
                .status(request.status())
                .priority(request.priority())
                .deadline(request.deadline())
                .project(project)
                .assignee(assignee)
                .build();

        TaskEntity savedTask = taskRepository.save(task);
        projectStatisticsCacheService.evictProjectStatistics(projectId);
        return TaskMapper.toResponse(savedTask);
    }

    public List<TaskResponse> getTasksByProjectId(
            Long projectId,
            TaskStatus status,
            TaskPriority priority
    ) {
        projectService.getProjectEntityById(projectId);

        List<TaskEntity> tasks;

        if (status == null && priority == null) {
            tasks = taskRepository.findByProjectId(projectId);
        } else if (status != null && priority == null) {
            tasks = taskRepository.findByProjectIdAndStatus(projectId, status);
        } else if (status == null) {
            tasks = taskRepository.findByProjectIdAndPriority(projectId, priority);
        } else {
            tasks = taskRepository.findByProjectIdAndStatusAndPriority(projectId, status, priority);
        }

        return tasks
                .stream()
                .map(TaskMapper::toResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id) {
        return TaskMapper.toResponse(getTaskEntityById(id));
    }

    @Transactional
    public TaskResponse updateTask(Long id, UpdateTaskRequest request) {
        TaskEntity task = getTaskEntityById(id);
        Long projectId = task.getProject().getId();

        if (request.title() != null) {
            task.setTitle(request.title());
        }

        if (request.description() != null) {
            task.setDescription(request.description());
        }

        if (request.priority() != null) {
            task.setPriority(request.priority());
        }

        if (request.deadline() != null) {
            task.setDeadline(request.deadline());
        }

        if (request.assigneeId() != null) {
            task.setAssignee(userService.getUserEntityById(request.assigneeId()));
        }

        TaskEntity updatedTask = taskRepository.save(task);
        projectStatisticsCacheService.evictProjectStatistics(projectId);
        return TaskMapper.toResponse(updatedTask);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, UpdateTaskStatusRequest request) {
        TaskEntity task = getTaskEntityById(id);
        Long projectId = task.getProject().getId();

        task.setStatus(request.status());

        TaskEntity updatedTask = taskRepository.save(task);
        projectStatisticsCacheService.evictProjectStatistics(projectId);
        return TaskMapper.toResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        TaskEntity task = getTaskEntityById(id);
        Long projectId = task.getProject().getId();

        taskRepository.delete(task);
        projectStatisticsCacheService.evictProjectStatistics(projectId);
    }

    public TaskEntity getTaskEntityById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new BaseException(
                        BusinessError.TASK_NOT_FOUND,
                        "Task with id " + id + " not found"
                ));
    }

    private UserEntity getAssignee(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }

        return userService.getUserEntityById(assigneeId);
    }
}
