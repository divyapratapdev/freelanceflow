package com.freelanceflow.aiquery.tools;

import com.freelanceflow.common.enums.ProjectStatus;
import com.freelanceflow.project.ProjectService;
import com.freelanceflow.project.dto.ProjectResponse;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service("getProjectsTool")
@Description("Fetch projects for the current user, optionally filtered by status if provided.")
public class GetProjectsTool implements Function<GetProjectsTool.Request, List<ProjectResponse>> {

    private final ProjectService projectService;

    public GetProjectsTool(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Override
    public List<ProjectResponse> apply(Request request) {
        ProjectStatus statusObj = null;
        if (request.status() != null && !request.status().isBlank()) {
            try {
                statusObj = ProjectStatus.valueOf(request.status().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid status for tool
            }
        }
        return projectService.listAll(request.userId(), statusObj, Pageable.unpaged()).getContent();
    }

    public record Request(Long userId, String status) {}
}
