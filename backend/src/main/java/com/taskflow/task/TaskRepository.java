package com.taskflow.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
        SELECT t FROM Task t
        WHERE t.project.id = :projectId
          AND (:status IS NULL OR t.status = :status)
          AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
        """)
    List<Task> findByProjectIdFiltered(
        @Param("projectId") UUID projectId,
        @Param("status") TaskStatus status,
        @Param("assigneeId") UUID assigneeId
    );
}