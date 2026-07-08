package com.taskflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.taskflow.kafka.event.TaskStatusChangedEvent;
import com.taskflow.kafka.producer.TaskEventProducer;
import com.taskflow.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private ProjectStatisticsCacheService projectStatisticsCacheService;

    @Mock
    private TaskEventProducer taskEventProducer;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_shouldCreateTask_withAssignee() {
        UserEntity owner = createUser(1L);
        UserEntity assignee = createUser(2L);
        ProjectEntity project = createProject(1L, owner);
        CreateTaskRequest request = new CreateTaskRequest(
                "Task",
                "Description",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                null,
                assignee.getId()
        );
        TaskEntity savedTask = createTask(1L, "Task", TaskStatus.TODO, TaskPriority.HIGH, project, assignee);

        when(projectService.getProjectEntityById(project.getId())).thenReturn(project);
        when(userService.getUserEntityById(assignee.getId())).thenReturn(assignee);
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(project.getId(), request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Task");
        assertThat(response.assignee().id()).isEqualTo(assignee.getId());
        verify(taskRepository).save(any(TaskEntity.class));
        verify(projectStatisticsCacheService).evictProjectStatistics(project.getId());
    }

    @Test
    void createTask_shouldCreateTask_withoutAssignee() {
        UserEntity owner = createUser(1L);
        ProjectEntity project = createProject(1L, owner);
        CreateTaskRequest request = new CreateTaskRequest(
                "Task",
                "Description",
                TaskStatus.TODO,
                TaskPriority.HIGH,
                null,
                null
        );
        TaskEntity savedTask = createTask(1L, "Task", TaskStatus.TODO, TaskPriority.HIGH, project, null);

        when(projectService.getProjectEntityById(project.getId())).thenReturn(project);
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(project.getId(), request);

        assertThat(response.assignee()).isNull();
        verify(userService, never()).getUserEntityById(any());
        verify(projectStatisticsCacheService).evictProjectStatistics(project.getId());
    }

    @Test
    void getTasksByProjectId_shouldReturnAllTasks_whenNoFilters() {
        ProjectEntity project = createProject(1L, createUser(1L));
        TaskEntity task = createTask(1L, "Task", TaskStatus.TODO, TaskPriority.HIGH, project, null);

        when(projectService.getProjectEntityById(project.getId())).thenReturn(project);
        when(taskRepository.findByProjectId(project.getId())).thenReturn(List.of(task));

        List<TaskResponse> response = taskService.getTasksByProjectId(project.getId(), null, null);

        assertThat(response).hasSize(1);
        verify(taskRepository).findByProjectId(project.getId());
    }

    @Test
    void getTasksByProjectId_shouldFilterByStatus() {
        ProjectEntity project = createProject(1L, createUser(1L));

        when(projectService.getProjectEntityById(project.getId())).thenReturn(project);
        when(taskRepository.findByProjectIdAndStatus(project.getId(), TaskStatus.TODO)).thenReturn(List.of());

        taskService.getTasksByProjectId(project.getId(), TaskStatus.TODO, null);

        verify(taskRepository).findByProjectIdAndStatus(project.getId(), TaskStatus.TODO);
    }

    @Test
    void getTasksByProjectId_shouldFilterByPriority() {
        ProjectEntity project = createProject(1L, createUser(1L));

        when(projectService.getProjectEntityById(project.getId())).thenReturn(project);
        when(taskRepository.findByProjectIdAndPriority(project.getId(), TaskPriority.HIGH)).thenReturn(List.of());

        taskService.getTasksByProjectId(project.getId(), null, TaskPriority.HIGH);

        verify(taskRepository).findByProjectIdAndPriority(project.getId(), TaskPriority.HIGH);
    }

    @Test
    void getTasksByProjectId_shouldFilterByStatusAndPriority() {
        ProjectEntity project = createProject(1L, createUser(1L));

        when(projectService.getProjectEntityById(project.getId())).thenReturn(project);
        when(taskRepository.findByProjectIdAndStatusAndPriority(
                project.getId(),
                TaskStatus.TODO,
                TaskPriority.HIGH
        )).thenReturn(List.of());

        taskService.getTasksByProjectId(project.getId(), TaskStatus.TODO, TaskPriority.HIGH);

        verify(taskRepository).findByProjectIdAndStatusAndPriority(
                project.getId(),
                TaskStatus.TODO,
                TaskPriority.HIGH
        );
    }

    @Test
    void getTaskById_shouldReturnTask_whenTaskExists() {
        ProjectEntity project = createProject(1L, createUser(1L));
        TaskEntity task = createTask(1L, "Task", TaskStatus.TODO, TaskPriority.HIGH, project, null);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(task.getId());

        assertThat(response.id()).isEqualTo(task.getId());
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void getTaskById_shouldThrowException_whenTaskDoesNotExist() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(1L))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getError()).isEqualTo(BusinessError.TASK_NOT_FOUND)
                );
    }

    @Test
    void updateTask_shouldUpdateOnlyProvidedFields() {
        UserEntity owner = createUser(1L);
        UserEntity assignee = createUser(2L);
        ProjectEntity project = createProject(1L, owner);
        TaskEntity task = createTask(1L, "Old", TaskStatus.TODO, TaskPriority.LOW, project, null);
        LocalDateTime deadline = LocalDateTime.now().plusDays(1);
        UpdateTaskRequest request = new UpdateTaskRequest(
                "New",
                "New description",
                TaskPriority.HIGH,
                deadline,
                assignee.getId()
        );

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(userService.getUserEntityById(assignee.getId())).thenReturn(assignee);
        when(taskRepository.save(task)).thenReturn(task);

        TaskResponse response = taskService.updateTask(task.getId(), request);

        assertThat(response.title()).isEqualTo("New");
        assertThat(response.description()).isEqualTo("New description");
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(response.deadline()).isEqualTo(deadline);
        assertThat(response.assignee().id()).isEqualTo(assignee.getId());
        verify(projectStatisticsCacheService).evictProjectStatistics(project.getId());
    }

    @Test
    void updateTaskStatus_shouldUpdateStatusEvictCacheAndSendKafkaEvent() {
        ProjectEntity project = createProject(1L, createUser(1L));
        TaskEntity task = createTask(1L, "Task", TaskStatus.TODO, TaskPriority.HIGH, project, null);
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));
        when(taskRepository.saveAndFlush(task)).thenReturn(task);

        TaskResponse response = taskService.updateTaskStatus(task.getId(), request);

        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(projectStatisticsCacheService).evictProjectStatistics(project.getId());

        ArgumentCaptor<TaskStatusChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(TaskStatusChangedEvent.class);
        verify(taskEventProducer).sendTaskStatusChangedEvent(eventCaptor.capture());

        TaskStatusChangedEvent event = eventCaptor.getValue();
        assertThat(event.taskId()).isEqualTo(task.getId());
        assertThat(event.projectId()).isEqualTo(project.getId());
        assertThat(event.eventType()).isEqualTo("TASK_STATUS_CHANGED");
        assertThat(event.oldStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(event.newStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void deleteTask_shouldDeleteTaskAndEvictCache_whenTaskExists() {
        ProjectEntity project = createProject(1L, createUser(1L));
        TaskEntity task = createTask(1L, "Task", TaskStatus.TODO, TaskPriority.HIGH, project, null);

        when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

        taskService.deleteTask(task.getId());

        verify(taskRepository).delete(task);
        verify(projectStatisticsCacheService).evictProjectStatistics(project.getId());
    }

    @Test
    void deleteTask_shouldThrowException_whenTaskDoesNotExist() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(1L))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getError()).isEqualTo(BusinessError.TASK_NOT_FOUND)
                );

        verify(taskRepository, never()).delete(any(TaskEntity.class));
        verify(projectStatisticsCacheService, never()).evictProjectStatistics(any());
    }

    private UserEntity createUser(Long id) {
        return UserEntity.builder()
                .id(id)
                .username("user" + id)
                .email("user" + id + "@example.com")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ProjectEntity createProject(Long id, UserEntity owner) {
        LocalDateTime now = LocalDateTime.now();

        return ProjectEntity.builder()
                .id(id)
                .name("Project")
                .description("Description")
                .owner(owner)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private TaskEntity createTask(
            Long id,
            String title,
            TaskStatus status,
            TaskPriority priority,
            ProjectEntity project,
            UserEntity assignee
    ) {
        LocalDateTime now = LocalDateTime.now();

        return TaskEntity.builder()
                .id(id)
                .title(title)
                .description("Description")
                .status(status)
                .priority(priority)
                .deadline(null)
                .project(project)
                .assignee(assignee)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
