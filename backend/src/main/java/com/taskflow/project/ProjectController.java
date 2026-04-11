package com.taskflow.project;

import com.taskflow.project.dto.CreateProjectRequest;
import com.taskflow.project.dto.ProjectResponse;
import com.taskflow.project.dto.UpdateProjectRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectResponse> listProjects(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return projectService.listProjects(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody CreateProjectRequest request,
                                         Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return projectService.createProject(request, userId);
    }

    @GetMapping("/{id}")
    public ProjectResponse getProject(@PathVariable UUID id,
                                      Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return projectService.getProject(id, userId);
    }

    @PatchMapping("/{id}")
    public ProjectResponse updateProject(@PathVariable UUID id,
                                         @Valid @RequestBody UpdateProjectRequest request,
                                         Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return projectService.updateProject(id, request, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable UUID id,
                              Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        projectService.deleteProject(id, userId);
    }
}