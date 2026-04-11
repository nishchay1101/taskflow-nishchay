package com.taskflow.task.dto;

import com.taskflow.task.Task;
import com.taskflow.task.TaskPriority;
import com.taskflow.task.TaskStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        UUID projectId,
        UUID assigneeId,
        UUID creatorId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getAssignee() != null ? task.getAssignee().getId() : null,
                task.getCreator().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}