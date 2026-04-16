package com.freelanceflow.project;

import com.freelanceflow.common.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByUserId(Long userId, Pageable pageable);

    Page<Project> findByUserIdAndStatus(Long userId, ProjectStatus status, Pageable pageable);

    Page<Project> findByUserIdAndClientId(Long userId, Long clientId, Pageable pageable);
}
