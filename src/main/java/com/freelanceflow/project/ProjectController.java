package com.freelanceflow.project;

import com.freelanceflow.common.ApiResponse;
import com.freelanceflow.common.PageResponse;
import com.freelanceflow.common.SecurityUtils;
import com.freelanceflow.common.enums.ProjectStatus;
import com.freelanceflow.project.dto.ProjectRequest;
import com.freelanceflow.project.dto.ProjectResponse;
import com.freelanceflow.project.dto.ProjectUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Manage freelancer projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponse>> create(
            @RequestBody @Valid ProjectRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(projectService.create(userId, request)));
    }

    @GetMapping
    @Operation(summary = "List all projects (paginated), optional status filter")
    public ResponseEntity<ApiResponse<PageResponse<ProjectResponse>>> listAll(
            @RequestParam(required = false) ProjectStatus status,
            Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                PageResponse.of(projectService.listAll(userId, status, pageable))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getById(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(projectService.getById(userId, id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project")
    public ResponseEntity<ApiResponse<ProjectResponse>> update(
            @PathVariable Long id,
            @RequestBody ProjectUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(projectService.update(userId, id, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete project")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        projectService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
