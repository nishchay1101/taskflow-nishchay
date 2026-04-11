package com.taskflow.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @Query("""
        SELECT DISTINCT p FROM Project p
        JOIN FETCH p.owner
        WHERE p.owner.id = :userId
           OR EXISTS (
               SELECT t FROM Task t
               WHERE t.project = p AND t.assignee.id = :userId
           )
        ORDER BY p.createdAt DESC
        """)
    List<Project> findAllAccessibleByUserId(@Param("userId") UUID userId,
                                            Pageable pageable);
    @Query("""
        SELECT p FROM Project p
        JOIN FETCH p.owner
        WHERE p.id = :id
        """)
    Optional<Project> findByIdWithOwner(@Param("id") UUID id);

    @Query("""
        SELECT COUNT(DISTINCT p) FROM Project p
        WHERE p.owner.id = :userId
          OR EXISTS (
              SELECT t FROM Task t
              WHERE t.project = p AND t.assignee.id = :userId
          )
        """)
    long countAllAccessibleByUserId(@Param("userId") UUID userId);

}