package com.taskflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskflow.dto.response.ProjectStatisticsResponse;
import com.taskflow.entity.ProjectEntity;
import com.taskflow.entity.TaskEntity;
import com.taskflow.entity.UserEntity;
import com.taskflow.enums.TaskPriority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.exception.BaseException;
import com.taskflow.exception.BusinessError;
import com.taskflow.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class ProjectStatisticsServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ProjectStatisticsService projectStatisticsService;

    @Test
    void getProjectStatistics_shouldCalculateCorrectStatistics() {
        Long projectId = 1L;
        ProjectEntity project = createProject(projectId);
        LocalDateTime now = LocalDateTime.now();
        List<TaskEntity> tasks = List.of(
                createTask(1L, TaskStatus.TODO, TaskPriority.HIGH, now.minusDays(1), project),
                createTask(2L, TaskStatus.TODO, TaskPriority.LOW, now.plusDays(1), project),
                createTask(3L, TaskStatus.IN_PROGRESS, TaskPriority.HIGH, now.plusDays(1), project),
                createTask(4L, TaskStatus.DONE, TaskPriority.MEDIUM, now.minusDays(1), project)
        );

        when(projectService.getProjectEntityById(projectId)).thenReturn(project);
        when(taskRepository.findByProjectId(projectId)).thenReturn(tasks);

        ProjectStatisticsResponse response = projectStatisticsService.getProjectStatistics(projectId);

        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.totalTasks()).isEqualTo(4);
        assertThat(response.todo()).isEqualTo(2);
        assertThat(response.inProgress()).isEqualTo(1);
        assertThat(response.done()).isEqualTo(1);
        assertThat(response.highPriority()).isEqualTo(2);
        assertThat(response.overdue()).isEqualTo(1);
    }

    @Test
    void getProjectStatistics_shouldReturnZeroCounts_whenProjectHasNoTasks() {
        Long projectId = 1L;
        ProjectEntity project = createProject(projectId);

        when(projectService.getProjectEntityById(projectId)).thenReturn(project);
        when(taskRepository.findByProjectId(projectId)).thenReturn(List.of());

        ProjectStatisticsResponse response = projectStatisticsService.getProjectStatistics(projectId);

        assertThat(response.projectId()).isEqualTo(projectId);
        assertThat(response.totalTasks()).isZero();
        assertThat(response.todo()).isZero();
        assertThat(response.inProgress()).isZero();
        assertThat(response.done()).isZero();
        assertThat(response.highPriority()).isZero();
        assertThat(response.overdue()).isZero();
    }

    @Test
    void getProjectStatistics_shouldThrowException_whenProjectDoesNotExist() {
        Long projectId = 1L;

        when(projectService.getProjectEntityById(projectId))
                .thenThrow(new BaseException(BusinessError.PROJECT_NOT_FOUND));

        assertThatThrownBy(() -> projectStatisticsService.getProjectStatistics(projectId))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getError()).isEqualTo(BusinessError.PROJECT_NOT_FOUND)
                );

        verify(taskRepository, never()).findByProjectId(projectId);
    }

    private UserEntity createUser(Long id) {
        return UserEntity.builder()
                .id(id)
                .username("owner")
                .email("owner@example.com")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ProjectEntity createProject(Long id) {
        LocalDateTime now = LocalDateTime.now();

        return ProjectEntity.builder()
                .id(id)
                .name("Project")
                .description("Description")
                .owner(createUser(1L))
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private TaskEntity createTask(
            Long id,
            TaskStatus status,
            TaskPriority priority,
            LocalDateTime deadline,
            ProjectEntity project
    ) {
        LocalDateTime now = LocalDateTime.now();

        return TaskEntity.builder()
                .id(id)
                .title("Task " + id)
                .description("Description")
                .status(status)
                .priority(priority)
                .deadline(deadline)
                .project(project)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
