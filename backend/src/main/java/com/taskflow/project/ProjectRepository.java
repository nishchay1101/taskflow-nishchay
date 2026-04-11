package com.taskflow.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByOwnerId(UUID ownerId);

    @Query("""
        SELECT DISTINCT p FROM Project p
        LEFT JOIN Task t ON t.project = p
        WHERE p.owner.id = :userId
           OR t.assignee.id = :userId
        """)
    List<Project> findAllAccessibleByUserId(@Param("userId") UUID userId);
}