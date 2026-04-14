package com.freelanceflow.ai;

/**
 * Interface for AI completions. 
 * Allows switching between real providers (Grok/Groq) and demo/mock implementations.
 */
public interface AiClient {
    String getCompletion(String systemPrompt, String userMessage);
}
