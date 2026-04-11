package com.taskflow.task.dto;

import com.taskflow.task.TaskPriority;
import com.taskflow.task.TaskStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public record UpdateTaskRequest(
        Optional<String> title,
        Optional<String> description,
        Optional<TaskStatus> status,
        Optional<TaskPriority> priority,
        Optional<UUID> assigneeId,    // Optional.of(null) not possible — null inside Optional means "clear assignee"
        Optional<LocalDate> dueDate
) {}