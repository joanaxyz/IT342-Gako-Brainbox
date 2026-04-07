package edu.cit.gako.brainbox.ai.provider;

import edu.cit.gako.brainbox.ai.dto.response.SpeechTranscriptionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Adapter target interface for AI provider communication.
 *
 * ProxyProvider is the concrete Adapter that translates BrainBox calls
 * into the OpenAI-compatible HTTP API format. Introducing this interface
 * decouples AiService from the HTTP implementation details and makes the
 * provider swappable (e.g., direct OpenAI, local Ollama, mock for tests).
 */
public interface AiProvider {

    /**
     * Send a chat completion request and return the assistant message content.
     */
    String generateResponse(String proxyUrl, String apiKey, String model,
                            List<Map<String, String>> messages, double temperature);

    /**
     * Transcribe an audio file and return the text result.
     */
    SpeechTranscriptionResponse transcribeAudio(String proxyUrl, String apiKey,
                                                MultipartFile file, String language);
}
