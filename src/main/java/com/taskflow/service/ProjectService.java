package com.taskflow.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.entity.ProjectEntity;
import com.taskflow.entity.UserEntity;
import com.taskflow.exception.BaseException;
import com.taskflow.exception.BusinessError;
import com.taskflow.mapper.ProjectMapper;
import com.taskflow.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        UserEntity owner = userService.getUserEntityById(request.ownerId());

        ProjectEntity project = ProjectEntity.builder()
                .name(request.name())
                .description(request.description())
                .owner(owner)
                .build();

        ProjectEntity savedProject = projectRepository.save(project);
        return ProjectMapper.toResponse(savedProject);
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(ProjectMapper::toResponse)
                .toList();
    }

    public ProjectResponse getProjectById(Long id) {
        return ProjectMapper.toResponse(getProjectEntityById(id));
    }

    public List<ProjectResponse> getProjectsByOwnerId(Long ownerId) {
        userService.getUserEntityById(ownerId);

        return projectRepository.findByOwnerId(ownerId)
                .stream()
                .map(ProjectMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new BaseException(
                    BusinessError.PROJECT_NOT_FOUND,
                    "Project with id " + id + " not found"
            );
        }

        projectRepository.deleteById(id);
    }

    public ProjectEntity getProjectEntityById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BaseException(
                        BusinessError.PROJECT_NOT_FOUND,
                        "Project with id " + id + " not found"
                ));
    }
}
