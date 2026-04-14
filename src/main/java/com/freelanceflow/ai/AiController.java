package com.freelanceflow.ai;

import com.freelanceflow.ai.dto.AiInsightResponse;
import com.freelanceflow.common.ApiResponse;
import com.freelanceflow.common.PageResponse;
import com.freelanceflow.common.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Insights", description = "AI-generated business insights")
public class AiController {

    private final AiInvoiceAnalyzerService analyzerService;

    public AiController(AiInvoiceAnalyzerService analyzerService) {
        this.analyzerService = analyzerService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Manually trigger AI insight generation for user")
    public ResponseEntity<ApiResponse<Void>> triggerAnalysis() {
        Long userId = SecurityUtils.getCurrentUserId();
        // This is Async
        analyzerService.analyzeInvoicesForUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/insights")
    @Operation(summary = "List all AI insights for user")
    public ResponseEntity<ApiResponse<PageResponse<AiInsightResponse>>> getInsights(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResponse<AiInsightResponse> response = PageResponse.of(
                analyzerService.getInsights(userId, pageable).map(AiInsightResponse::from));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
