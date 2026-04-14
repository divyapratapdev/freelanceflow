package com.freelanceflow.aiquery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// Spring AI MCP support requires snapshot dependencies right now, 
// so we stub the class configuration if they were available, ensuring the Spring Boot app compiles perfectly.
// In a real Spring AI implementation, this would use @Bean for McpSyncClient or similar.
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.mcp.enabled", havingValue = "true")
public class FreelanceFlowMcpServer {
    // Stub for MCP (Model Context Protocol).
    // All Tools (GetInvoicesTool, etc.) are already defined as Spring Beans and @Service classes.
    // If Spring AI's ChatModel was configured, they could be injected as:
    // ChatResponse response = chatModel.call(new Prompt(msg, SpringAiOptions.builder().withFunction("getInvoicesTool").build()));
}
