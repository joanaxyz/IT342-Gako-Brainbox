package edu.cit.gako.brainbox.modules.ai.provider;

import java.util.List;
import java.util.Map;

/**
 * Adapter target interface for AI provider communication.
 *
 * OpenAiCompatibleProvider translates BrainBox calls into the OpenAI-compatible
 * HTTP API format. Introducing this interface decouples AiService from the HTTP
 * implementation details and makes the provider swappable.
 */
public interface AiProvider {

    /**
     * Send a chat completion request and return the assistant message content.
     */
    String generateResponse(String baseUrl, String apiKey, String model,
                            List<Map<String, String>> messages, double temperature);
}
