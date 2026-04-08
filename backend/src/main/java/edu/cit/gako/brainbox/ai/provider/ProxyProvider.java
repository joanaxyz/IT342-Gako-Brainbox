package edu.cit.gako.brainbox.ai.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * Concrete Adapter: adapts the OpenAI-compatible HTTP proxy to the AiProvider interface.
 * AiService depends only on AiProvider, keeping it independent of HTTP details.
 */
@Component
@RequiredArgsConstructor
public class ProxyProvider implements AiProvider {

    private final ObjectMapper objectMapper;

    public String generateResponse(String proxyUrl, String apiKey, String model,
                                   List<Map<String, String>> messages, double temperature) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);

            String json = objectMapper.writeValueAsString(requestBody);
            String url = proxyUrl + "/chat/completions";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("AI proxy error (" + response.statusCode() + "): " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error calling AI proxy", e);
        }
    }

}
