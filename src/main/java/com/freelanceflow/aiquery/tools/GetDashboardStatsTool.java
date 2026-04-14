package com.freelanceflow.aiquery.tools;

import com.freelanceflow.dashboard.DashboardService;
import com.freelanceflow.dashboard.dto.DashboardResponse;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service("getDashboardStatsTool")
@Description("Fetch high level dashboard statistics including revenue, outstanding amounts, and active projects.")
public class GetDashboardStatsTool implements Function<Long, DashboardResponse> {

    private final DashboardService dashboardService;

    public GetDashboardStatsTool(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public DashboardResponse apply(Long userId) {
        return dashboardService.getDashboardSummary(userId);
    }
}
