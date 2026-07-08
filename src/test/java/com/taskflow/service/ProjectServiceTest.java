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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.entity.ProjectEntity;
import com.taskflow.entity.UserEntity;
import com.taskflow.exception.BaseException;
import com.taskflow.exception.BusinessError;
import com.taskflow.repository.ProjectRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void createProject_shouldCreateProject_whenOwnerExists() {
        UserEntity owner = createUser(1L);
        CreateProjectRequest request = new CreateProjectRequest("Project", "Description", owner.getId());
        ProjectEntity savedProject = createProject(1L, "Project", "Description", owner);

        when(userService.getUserEntityById(owner.getId())).thenReturn(owner);
        when(projectRepository.save(any(ProjectEntity.class))).thenReturn(savedProject);

        ProjectResponse response = projectService.createProject(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Project");
        assertThat(response.description()).isEqualTo("Description");
        assertThat(response.owner().id()).isEqualTo(owner.getId());
        verify(projectRepository).save(any(ProjectEntity.class));
    }

    @Test
    void createProject_shouldThrowException_whenOwnerDoesNotExist() {
        CreateProjectRequest request = new CreateProjectRequest("Project", "Description", 1L);

        when(userService.getUserEntityById(1L)).thenThrow(new BaseException(BusinessError.USER_NOT_FOUND));

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getError()).isEqualTo(BusinessError.USER_NOT_FOUND)
                );

        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    void getProjectById_shouldReturnProject_whenProjectExists() {
        UserEntity owner = createUser(1L);
        ProjectEntity project = createProject(1L, "Project", "Description", owner);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProjectById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Project");
        assertThat(response.owner().id()).isEqualTo(owner.getId());
    }

    @Test
    void getProjectById_shouldThrowException_whenProjectDoesNotExist() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(1L))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getError()).isEqualTo(BusinessError.PROJECT_NOT_FOUND)
                );
    }

    @Test
    void getProjectsByOwnerId_shouldReturnProjects_whenOwnerExists() {
        UserEntity owner = createUser(1L);
        ProjectEntity project = createProject(1L, "Project", "Description", owner);

        when(userService.getUserEntityById(owner.getId())).thenReturn(owner);
        when(projectRepository.findByOwnerId(owner.getId())).thenReturn(List.of(project));

        List<ProjectResponse> response = projectService.getProjectsByOwnerId(owner.getId());

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(project.getId());
    }

    @Test
    void deleteProject_shouldDeleteProject_whenProjectExists() {
        when(projectRepository.existsById(1L)).thenReturn(true);

        projectService.deleteProject(1L);

        verify(projectRepository).deleteById(1L);
    }

    @Test
    void deleteProject_shouldThrowException_whenProjectDoesNotExist() {
        when(projectRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> projectService.deleteProject(1L))
                .isInstanceOfSatisfying(BaseException.class, exception ->
                        assertThat(exception.getError()).isEqualTo(BusinessError.PROJECT_NOT_FOUND)
                );

        verify(projectRepository, never()).deleteById(1L);
    }

    @Test
    void getProjectEntityById_shouldReturnEntity_whenProjectExists() {
        UserEntity owner = createUser(1L);
        ProjectEntity project = createProject(1L, "Project", "Description", owner);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectEntity response = projectService.getProjectEntityById(1L);

        assertThat(response).isSameAs(project);
    }

    private UserEntity createUser(Long id) {
        return UserEntity.builder()
                .id(id)
                .username("owner")
                .email("owner@example.com")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private ProjectEntity createProject(Long id, String name, String description, UserEntity owner) {
        LocalDateTime now = LocalDateTime.now();

        return ProjectEntity.builder()
                .id(id)
                .name(name)
                .description(description)
                .owner(owner)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
