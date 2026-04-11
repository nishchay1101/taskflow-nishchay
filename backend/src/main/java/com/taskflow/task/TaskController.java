package com.taskflow.task;

import com.taskflow.task.dto.CreateTaskRequest;
import com.taskflow.task.dto.TaskResponse;
import com.taskflow.task.dto.UpdateTaskRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.taskflow.common.PagedResponse;
import com.taskflow.task.dto.TaskStatsResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping("/projects/{projectId}/tasks")
    public PagedResponse<TaskResponse> listTasks(
            @PathVariable UUID projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) UUID assignee,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return taskService.listTasks(projectId, status, assignee, page, limit);
    }

    @PostMapping("/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest req,
            Authentication auth) {
        UUID currentUserId = UUID.fromString(auth.getName());
        return taskService.createTask(projectId, req, currentUserId);
    }

    @PatchMapping("/tasks/{taskId}")
    public TaskResponse updateTask(
            @PathVariable UUID taskId,
            @RequestBody UpdateTaskRequest req,
            Authentication auth) {
        UUID currentUserId = UUID.fromString(auth.getName());
        return taskService.updateTask(taskId, req, currentUserId);
    }

    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @PathVariable UUID taskId,
            Authentication auth) {
        UUID currentUserId = UUID.fromString(auth.getName());
        taskService.deleteTask(taskId, currentUserId);
    }

    @GetMapping("/projects/{projectId}/stats")
    public TaskStatsResponse getStats(
            @PathVariable UUID projectId,
            Authentication authentication) {
        return taskService.getStats(projectId);
    }
}