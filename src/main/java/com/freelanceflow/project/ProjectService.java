package com.freelanceflow.project;

import com.freelanceflow.client.ClientRepository;
import com.freelanceflow.common.CacheConstants;
import com.freelanceflow.common.enums.ProjectStatus;
import com.freelanceflow.project.dto.ProjectRequest;
import com.freelanceflow.project.dto.ProjectResponse;
import com.freelanceflow.project.dto.ProjectUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ClientRepository clientRepository) {
        this.projectRepository = projectRepository;
        this.clientRepository = clientRepository;
    }

    @Transactional
    @CacheEvict(value = CacheConstants.DASHBOARD, key = "#userId")
    public ProjectResponse create(Long userId, ProjectRequest request) {
        MDC.put("userId", String.valueOf(userId));
        try {
            // Verify client belongs to this user
            if (!clientRepository.existsByIdAndUserId(request.getClientId(), userId)) {
                throw new EntityNotFoundException("Client not found: " + request.getClientId());
            }
            Project project = new Project();
            project.setUserId(userId);
            project.setClientId(request.getClientId());
            project.setName(request.getName());
            project.setDescription(request.getDescription());
            project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.NOT_STARTED);
            project.setBudget(request.getBudget());
            project.setStartDate(request.getStartDate());
            project.setEndDate(request.getEndDate());
            return ProjectResponse.from(projectRepository.save(project));
        } finally {
            MDC.clear();
        }
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> listAll(Long userId, ProjectStatus status, Pageable pageable) {
        if (status != null) {
            return projectRepository.findByUserIdAndStatus(userId, status, pageable)
                    .map(ProjectResponse::from);
        }
        return projectRepository.findByUserId(userId, pageable).map(ProjectResponse::from);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(Long userId, Long projectId) {
        Project project = findAndVerifyOwnership(userId, projectId);
        return ProjectResponse.from(project);
    }

    @Transactional
    @CacheEvict(value = CacheConstants.DASHBOARD, key = "#userId")
    public ProjectResponse update(Long userId, Long projectId, ProjectUpdateRequest request) {
        MDC.put("userId", String.valueOf(userId));
        try {
            Project project = findAndVerifyOwnership(userId, projectId);
            if (request.getName() != null && !request.getName().isBlank()) {
                project.setName(request.getName());
            }
            if (request.getDescription() != null) project.setDescription(request.getDescription());
            if (request.getStatus() != null) project.setStatus(request.getStatus());
            if (request.getBudget() != null) project.setBudget(request.getBudget());
            if (request.getStartDate() != null) project.setStartDate(request.getStartDate());
            if (request.getEndDate() != null) project.setEndDate(request.getEndDate());
            return ProjectResponse.from(projectRepository.save(project));
        } finally {
            MDC.clear();
        }
    }

    @Transactional
    @CacheEvict(value = CacheConstants.DASHBOARD, key = "#userId")
    public void delete(Long userId, Long projectId) {
        MDC.put("userId", String.valueOf(userId));
        try {
            Project project = findAndVerifyOwnership(userId, projectId);
            project.setDeletedAt(Instant.now());
            projectRepository.save(project);
        } finally {
            MDC.clear();
        }
    }

    private Project findAndVerifyOwnership(Long userId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + projectId));
        if (!project.getUserId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }
        return project;
    }
}
