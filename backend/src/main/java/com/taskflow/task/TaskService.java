package com.taskflow.task;

import com.taskflow.common.exception.ForbiddenException;
import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.project.Project;
import com.taskflow.project.ProjectRepository;
import com.taskflow.task.dto.CreateTaskRequest;
import com.taskflow.task.dto.TaskResponse;
import com.taskflow.task.dto.UpdateTaskRequest;
import com.taskflow.user.User;
import com.taskflow.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskflow.common.PagedResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<TaskResponse> listTasks(UUID projectId, TaskStatus status,
                                                UUID assigneeId, int page, int limit) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }
        int clampedLimit = Math.min(limit, 100);
        Pageable pageable = PageRequest.of(page - 1, clampedLimit);

        List<TaskResponse> data = taskRepository
                .findByProjectIdFiltered(projectId, status, assigneeId, pageable)
                .stream()
                .map(TaskResponse::from)
                .toList();

        long total = taskRepository.countByProjectIdFiltered(projectId, status, assigneeId);
        return PagedResponse.of(data, page, clampedLimit, total);
    }

    @Transactional
    public TaskResponse createTask(UUID projectId, CreateTaskRequest req, UUID currentUserId) {
        Project project = projectRepository.findByIdWithOwner(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Only project owner can create tasks
        if (!project.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenException("Only the project owner can create tasks");
        }

        User creator = userRepository.getReferenceById(currentUserId);

        Task task = new Task();
        task.setTitle(req.title());
        task.setDescription(req.description());
        task.setStatus(TaskStatus.TODO);  // always default, ignore any client value
        task.setPriority(req.priority() != null ? req.priority() : TaskPriority.MEDIUM);
        task.setDueDate(req.dueDate());
        task.setProject(project);
        task.setCreator(creator);

        if (req.assigneeId() != null) {
            if (!userRepository.existsById(req.assigneeId())) {
                throw new ResourceNotFoundException("Assignee not found");
            }
            task.setAssignee(userRepository.getReferenceById(req.assigneeId()));
        }

        Task saved = taskRepository.save(task);
        // Re-fetch to populate all lazy associations for response
        return TaskResponse.from(taskRepository.findByIdWithAssociations(saved.getId()).orElseThrow());
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest req, UUID currentUserId) {
        Task task = taskRepository.findByIdWithAssociations(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        UUID projectOwnerId = task.getProject().getOwner().getId();
        UUID assigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;

        boolean isOwner = projectOwnerId.equals(currentUserId);
        boolean isAssignee = currentUserId.equals(assigneeId);

        if (!isOwner && !isAssignee) {
            throw new ForbiddenException("Only the project owner or task assignee can update this task");
        }

        // Apply only fields that were present in the request body
        if (req.title().isPresent()) {
            String newTitle = req.title().get();
            if (newTitle != null && !newTitle.isBlank()) {
                task.setTitle(newTitle);
            }
        }

        if (req.description().isPresent()) {
            task.setDescription(req.description().get());
        }

        if (req.status().isPresent() && req.status().get() != null) {
            task.setStatus(req.status().get());
        }

        if (req.priority().isPresent() && req.priority().get() != null) {
            task.setPriority(req.priority().get());
        }

        if (req.dueDate().isPresent()) {
            task.setDueDate(req.dueDate().get()); // null = clear due date
        }

        if (req.assigneeId().isPresent()) {
            UUID newAssigneeId = req.assigneeId().get();
            if (newAssigneeId == null) {
                task.setAssignee(null); // {"assigneeId": null} → clear assignee
            } else {
                if (!userRepository.existsById(newAssigneeId)) {
                    throw new ResourceNotFoundException("Assignee not found");
                }
                task.setAssignee(userRepository.getReferenceById(newAssigneeId));
            }
        }

        return TaskResponse.from(task); // @PreUpdate fires on transaction commit
    }

    @Transactional
    public void deleteTask(UUID taskId, UUID currentUserId) {
        Task task = taskRepository.findByIdWithAssociations(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        UUID projectOwnerId = task.getProject().getOwner().getId();
        UUID creatorId = task.getCreator().getId();

        boolean isOwner = projectOwnerId.equals(currentUserId);
        boolean isCreator = creatorId.equals(currentUserId);

        if (!isOwner && !isCreator) {
            throw new ForbiddenException("Only the project owner or task creator can delete this task");
        }

        taskRepository.delete(task);
    }
}