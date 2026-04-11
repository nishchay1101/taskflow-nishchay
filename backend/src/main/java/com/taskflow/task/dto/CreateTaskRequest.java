package com.taskflow.task.dto;

import com.taskflow.task.TaskPriority;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTaskRequest(

        @NotBlank(message = "title is required")
        String title,

        String description,

        TaskPriority priority,       // null → defaults to MEDIUM in service

        UUID assigneeId,             // null → unassigned
        
        LocalDate dueDate            // null → no due date; past dates allowed
) {}