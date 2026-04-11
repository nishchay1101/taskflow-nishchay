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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(UUID projectId, TaskStatus status, UUID assigneeId) {
        // Verify project exists
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }
        return taskRepository
                .findByProjectIdFiltered(projectId, status, assigneeId)
                .stream()
                .map(TaskResponse::from)
                .toList();
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
        if (req.title() != null && req.title().isPresent()) {
            String newTitle = req.title().get();
            if (newTitle != null && !newTitle.isBlank()) {
                task.setTitle(newTitle);
            }
        }
        if (req.description() != null && req.description().isPresent()) {
            task.setDescription(req.description().get()); // null = clear description
        }
        if (req.status() != null && req.status().isPresent()) {
            task.setStatus(req.status().get());
        }
        if (req.priority() != null && req.priority().isPresent()) {
            task.setPriority(req.priority().get());
        }
        if (req.dueDate() != null && req.dueDate().isPresent()) {
            task.setDueDate(req.dueDate().get()); // null = clear due date
        }
        if (req.assigneeId() != null && req.assigneeId().isPresent()) {
            UUID newAssigneeId = req.assigneeId().get();
            if (newAssigneeId == null) {
                task.setAssignee(null); // explicit clear
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