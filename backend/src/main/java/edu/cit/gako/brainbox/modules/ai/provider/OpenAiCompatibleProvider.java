package edu.cit.gako.brainbox.modules.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Backend provider client for OpenAI-compatible chat completions APIs.
 * AiService depends only on AiProvider, keeping it independent of HTTP details.
 */
@Component
@RequiredArgsConstructor
public class OpenAiCompatibleProvider implements AiProvider {

    private final ObjectMapper objectMapper;

    public String generateResponse(String baseUrl, String apiKey, String model,
                                   List<Map<String, String>> messages, double temperature) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);

            String json = objectMapper.writeValueAsString(requestBody);
            String url = buildChatCompletionsUrl(baseUrl);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(formatProviderError(response.statusCode(), response.body()));
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode error = root.path("error");
            if (!error.isMissingNode() && !error.isNull()) {
                throw new RuntimeException("AI provider error: " + extractErrorMessage(root));
            }

            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                throw new RuntimeException("AI provider response did not include choices[0].message.content");
            }

            JsonNode content = choices.get(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull()) {
                throw new RuntimeException("AI provider response did not include choices[0].message.content");
            }

            return content.asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error calling AI provider", e);
        }
    }

    private String buildChatCompletionsUrl(String baseUrl) {
        return normalizeBaseUrl(baseUrl) + "/chat/completions";
    }

    private String normalizeBaseUrl(String baseUrl) {
        String normalized = baseUrl == null ? "" : baseUrl.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("API Base URL is required");
        }
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            throw new IllegalArgumentException("API Base URL must start with http:// or https://");
        }

        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        String suffix = "/chat/completions";
        if (lower.endsWith(suffix)) {
            normalized = normalized.substring(0, normalized.length() - suffix.length());
            while (normalized.endsWith("/")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }
        }

        return normalized;
    }

    private String formatProviderError(int statusCode, String responseBody) {
        return "AI provider error (" + statusCode + "): " + extractErrorMessage(responseBody);
    }

    private String extractErrorMessage(String responseBody) {
        try {
            return extractErrorMessage(objectMapper.readTree(responseBody));
        } catch (Exception ignored) {
            return responseBody == null || responseBody.isBlank() ? "empty response body" : responseBody;
        }
    }

    private String extractErrorMessage(JsonNode root) {
        String errorMessage = root.path("error").path("message").asText("");
        if (!errorMessage.isBlank()) {
            return errorMessage;
        }

        String message = root.path("message").asText("");
        if (!message.isBlank()) {
            return message;
        }

        return root.toString();
    }
}
