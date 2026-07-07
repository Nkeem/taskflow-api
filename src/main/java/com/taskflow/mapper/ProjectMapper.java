package com.taskflow.mapper;

import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.entity.ProjectEntity;

public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static ProjectResponse toResponse(ProjectEntity project) {
        if (project == null) {
            return null;
        }

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                UserMapper.toResponse(project.getOwner()),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
