package com.freelanceflow.aiquery;

import com.freelanceflow.common.ApiResponse;
import com.freelanceflow.common.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-query")
@Tag(name = "AI Query", description = "Natural Language queries to the freelancer data")
public class AiQueryController {

    private final AiQueryService aiQueryService;

    public AiQueryController(AiQueryService aiQueryService) {
        this.aiQueryService = aiQueryService;
    }

    @PostMapping
    @Operation(summary = "Ask a natural language question about business data")
    public ResponseEntity<ApiResponse<String>> askQuery(@RequestBody Map<String, String> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String query = request.get("query");
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Query is required"));
        }
        
        String response = aiQueryService.executeQuery(userId, query);
        return ResponseEntity.ok(ApiResponse.ok(response != null ? response : "Sorry, I couldn't answer that right now."));
    }
}
