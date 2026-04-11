package com.taskflow.task.dto;

import com.taskflow.common.Patch;
import com.taskflow.task.TaskPriority;
import com.taskflow.task.TaskStatus;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateTaskRequest(
        Patch<String> title,
        Patch<String> description,
        Patch<TaskStatus> status,
        Patch<TaskPriority> priority,
        Patch<UUID> assigneeId,
        Patch<LocalDate> dueDate
) {
    // Compact constructor: absent fields arrive as null from Jackson
    // (field not in JSON at all = Jackson never sets it = null record component)
    // We normalize those to Patch.absent() so service code never null-checks
    public UpdateTaskRequest {
        title       = title       != null ? title       : Patch.absent();
        description = description != null ? description : Patch.absent();
        status      = status      != null ? status      : Patch.absent();
        priority    = priority    != null ? priority    : Patch.absent();
        assigneeId  = assigneeId  != null ? assigneeId  : Patch.absent();
        dueDate     = dueDate     != null ? dueDate     : Patch.absent();
    }
}