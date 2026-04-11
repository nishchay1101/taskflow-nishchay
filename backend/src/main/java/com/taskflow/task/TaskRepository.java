package com.taskflow.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Task t WHERE t.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") UUID projectId);
            
        // Count by status for a project
        @Query("""
                SELECT t.status, COUNT(t)
                FROM Task t
                WHERE t.project.id = :projectId
                GROUP BY t.status
                """)
        List<Object[]> countByStatus(@Param("projectId") UUID projectId);

        // Count by assignee for a project
        @Query("""
                SELECT a, COUNT(t)
                FROM Task t
                LEFT JOIN t.assignee a
                WHERE t.project.id = :projectId
                GROUP BY a
                """)
        List<Object[]> countByAssignee(@Param("projectId") UUID projectId);
}