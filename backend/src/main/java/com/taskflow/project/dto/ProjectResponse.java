package com.taskflow.project.dto;

import java.time.Instant;
import java.util.UUID;
import com.taskflow.project.Project;

public record ProjectResponse(
        UUID id,
        String name,
        String description,
        UUID ownerId,
        Instant createdAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getId(),
                project.getCreatedAt()
        );
    }
}