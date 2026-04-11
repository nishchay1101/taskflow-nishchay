package com.taskflow.project.dto;

import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(

        @Size(max = 200, message = "Project name must be under 200 characters")
        String name,

        String description
) {}