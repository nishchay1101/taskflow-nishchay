package com.taskflow.project;

import com.taskflow.common.exception.ForbiddenException;
import com.taskflow.common.exception.ResourceNotFoundException;
import com.taskflow.project.dto.CreateProjectRequest;
import com.taskflow.project.dto.ProjectResponse;
import com.taskflow.project.dto.UpdateProjectRequest;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> listProjects(UUID userId, int page, int limit) {
        int clampedLimit = Math.min(limit, 100);
        int offset = (page - 1) * clampedLimit;
        Pageable pageable = PageRequest.of(page - 1, clampedLimit);

        List<ProjectResponse> data = projectRepository
                .findAllAccessibleByUserId(userId, pageable)
                .stream()
                .map(ProjectResponse::from)
                .toList();

        long total = projectRepository.countAllAccessibleByUserId(userId);
        return PagedResponse.of(data, page, clampedLimit, total);
    }

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UUID userId) {
        User owner = userRepository.getReferenceById(userId);

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .owner(owner)
                .build();

        Project saved = projectRepository.save(project);
        projectRepository.flush(); // force DB write
        log.info("Project created: {} by user: {}", saved.getId(), userId);
        return toResponse(projectRepository.findByIdWithOwner(saved.getId()).orElseThrow());
    }

    public ProjectResponse getProject(UUID projectId, UUID userId) {
        Project project = findProjectOrThrow(projectId);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request, UUID userId) {
        Project project = findProjectOrThrow(projectId);

        if (!project.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the project owner can update this project");
        }

        if (request.name() != null && !request.name().isBlank()) {
            project.setName(request.name());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        log.info("Project updated: {} by user: {}", projectId, userId);
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(UUID projectId, UUID userId) {
        Project project = findProjectOrThrow(projectId);

        if (!project.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the project owner can delete this project");
        }
        log.info("Project deleted: {} by user: {}", projectId, userId);
        projectRepository.delete(project);
    }

    private Project findProjectOrThrow(UUID projectId) {
        return projectRepository.findByIdWithOwner(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Project not found: " + projectId));
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getId(),
                project.getCreatedAt()
        );
    }
}