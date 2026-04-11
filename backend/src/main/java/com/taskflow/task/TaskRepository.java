package com.taskflow.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Filtered listing — dynamic via JPQL, single query
    // status and assigneeId are optional filters
    @Query("""
            SELECT t FROM Task t
            JOIN FETCH t.project p
            LEFT JOIN FETCH t.assignee a
            JOIN FETCH t.creator c
            WHERE t.project.id = :projectId
              AND (:status IS NULL OR t.status = :status)
              AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
            ORDER BY t.createdAt DESC
            """)
    List<Task> findByProjectIdFiltered(
            @Param("projectId") UUID projectId,
            @Param("status") TaskStatus status,
            @Param("assigneeId") UUID assigneeId,
            Pageable pageable);

    // Single task fetch with all associations — used in update/delete
    @Query("""
            SELECT t FROM Task t
            JOIN FETCH t.project p
            JOIN FETCH p.owner o
            LEFT JOIN FETCH t.assignee a
            JOIN FETCH t.creator c
            WHERE t.id = :taskId
            """)
    Optional<Task> findByIdWithAssociations(@Param("taskId") UUID taskId);

    @Query("""
        SELECT COUNT(t) FROM Task t
        WHERE t.project.id = :projectId
          AND (:status IS NULL OR t.status = :status)
          AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
        """)
    long countByProjectIdFiltered(
            @Param("projectId") UUID projectId,
            @Param("status") TaskStatus status,
            @Param("assigneeId") UUID assigneeId);      
}