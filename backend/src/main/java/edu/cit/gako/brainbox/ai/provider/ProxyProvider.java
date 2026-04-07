package edu.cit.gako.brainbox.ai.provider;

import edu.cit.gako.brainbox.ai.dto.response.SpeechTranscriptionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Concrete Adapter: adapts the OpenAI-compatible HTTP proxy to the AiProvider interface.
 * AiService depends only on AiProvider, keeping it independent of HTTP details.
 */
@Component
@RequiredArgsConstructor
public class ProxyProvider implements AiProvider {

    private static final long MAX_TRANSCRIPTION_BYTES = 25L * 1024L * 1024L;

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

    public SpeechTranscriptionResponse transcribeAudio(String proxyUrl, String apiKey,
                                                        MultipartFile file, String language) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Audio file is required.");
            }
            if (file.getSize() > MAX_TRANSCRIPTION_BYTES) {
                throw new IllegalArgumentException("Audio file exceeds the 25 MB transcription limit.");
            }

            String boundary = "brainbox-" + UUID.randomUUID();
            List<byte[]> body = new ArrayList<>();

            appendFormField(body, boundary, "model", "whisper-1");
            if (language != null && !language.isBlank()) {
                appendFormField(body, boundary, "language", language.trim());
            }
            appendFormField(body, boundary, "response_format", "json");
            appendFileField(body, boundary, "file", file);
            body.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

            String url = proxyUrl + "/audio/transcriptions";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArrays(body))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Transcription proxy error (" + response.statusCode() + "): " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String text = root.path("text").asText("").trim();
            if (text.isBlank()) {
                throw new RuntimeException("Transcription returned empty result.");
            }

            return new SpeechTranscriptionResponse(text, "whisper-1");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error transcribing audio via proxy", e);
        }
    }

    private void appendFormField(List<byte[]> body, String boundary, String name, String value) {
        body.add(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.add(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.add((value + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    private void appendFileField(List<byte[]> body, String boundary, String name, MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank()
            ? file.getOriginalFilename() : "recording.webm";
        String contentType = file.getContentType() != null && !file.getContentType().isBlank()
            ? file.getContentType() : "audio/webm";

        body.add(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        body.add(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n"
            + "Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        body.add(file.getBytes());
        body.add("\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
