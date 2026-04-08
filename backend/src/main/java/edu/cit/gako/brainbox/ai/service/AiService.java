package edu.cit.gako.brainbox.ai.service;

import edu.cit.gako.brainbox.ai.dto.*;
import edu.cit.gako.brainbox.ai.dto.request.AiRequest;
import edu.cit.gako.brainbox.ai.dto.response.AiResponse;
import edu.cit.gako.brainbox.ai.entity.AiConfig;
import edu.cit.gako.brainbox.ai.prompt.AiPromptBuilder;
import edu.cit.gako.brainbox.ai.prompt.EditorModePromptBuilder;
import edu.cit.gako.brainbox.ai.prompt.ReviewModePromptBuilder;
import edu.cit.gako.brainbox.ai.provider.AiProvider;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.service.NotebookService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiService {

    private final NotebookService notebookService;
    private final AiConfigService aiConfigService;
    private final AiProvider proxyProvider;
    private final ObjectMapper objectMapper;

    private static final List<String> VALID_ACTIONS = List.of(
        "none", "add_to_editor", "replace_editor", "replace_selection", "replace_ai_selections", "create_quiz", "create_flashcard"
    );

    public AiResponse generateResponse(AiRequest aiRequest, Long userId) {
        try {
            AiConfig config = aiConfigService.getConfigEntity(userId);
            String apiKey = aiConfigService.decryptApiKey(config);

            Notebook notebook = notebookService.getNotebookByUuid(aiRequest.getNotebookUuid());
            String assistantMode = aiRequest.getMode() != null ? aiRequest.getMode().trim().toLowerCase() : "editor";
            boolean reviewMode = "review".equals(assistantMode);
            String notebookContent = notebook.getContent() != null ? notebook.getContent() : "";
            String context = notebookContent;
            if (context.isBlank()) {
                context = "(empty)";
            }

            String title = notebook.getTitle();
            String selectedText = aiRequest.getSelectedText() != null ? aiRequest.getSelectedText().trim() : "";
            String selectionMode = aiRequest.getSelectionMode() != null
                ? aiRequest.getSelectionMode().trim().toLowerCase() : "";
            List<AiSelectionTarget> aiSelections = normalizeAiSelections(aiRequest.getAiSelections());

            AiPromptBuilder promptBuilder = reviewMode
                ? new ReviewModePromptBuilder(title, context, selectedText, selectionMode, aiSelections)
                : new EditorModePromptBuilder(title, context, selectedText, selectionMode, aiSelections);
            String systemPrompt = promptBuilder.buildSystemPrompt();

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));

            if (aiRequest.getConversationHistory() != null) {
                for (Map<String, String> msg : aiRequest.getConversationHistory()) {
                    String role = msg.get("role");
                    String content = msg.get("content");
                    if (role != null && content != null && !content.isBlank()) {
                        messages.add(Map.of("role", role, "content", content));
                    }
                }
            }

            messages.add(Map.of("role", "user", "content", aiRequest.getQuery()));

            String aiMessage = proxyProvider.generateResponse(
                config.getProxyUrl(), apiKey, config.getModel(), messages, 0.4
            );
            AiResponse aiResponse = sanitizeMode(parseAiMessage(aiMessage), reviewMode);
            aiResponse.setConversationTitle(sanitizeConversationTitle(aiResponse.getConversationTitle(), aiRequest.getQuery()));
            return aiResponse;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error communicating with AI service", e);
        }
    }

    // ── Response parsing ──

    private AiResponse sanitizeMode(AiResponse response, boolean reviewMode) {
        if (!reviewMode) return response;
        String action = response.getAction();
        if ("add_to_editor".equals(action) || "replace_editor".equals(action)
            || "replace_selection".equals(action) || "replace_ai_selections".equals(action)) {
            response.setAction("none");
            response.setEditorContent(null);
            response.setSelectionEdits(null);
        }
        return response;
    }

    private AiResponse parseAiMessage(String aiMessage) {
        String trimmed = aiMessage.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            trimmed = trimmed.substring(start, end + 1);
        }

        try {
            JsonNode parsed = objectMapper.readTree(trimmed);
            String reply = parsed.path("reply").asText("");
            String action = parsed.path("action").asText("none");
            String editorContent = parsed.path("editorContent").asText("");
            String conversationTitle = parsed.path("conversationTitle").asText("");

            if (!VALID_ACTIONS.contains(action)) action = "none";
            if (reply.isBlank()) reply = extractFieldFallback(trimmed, "reply");
            if (conversationTitle.isBlank()) conversationTitle = extractFieldFallback(trimmed, "conversationTitle");

            Object quizData = null;
            Object flashcardData = null;
            List<AiSelectionEdit> selectionEdits = null;
            if ("create_quiz".equals(action) && !parsed.path("quizData").isMissingNode()) {
                quizData = objectMapper.convertValue(parsed.path("quizData"), Object.class);
            }
            if ("create_flashcard".equals(action) && !parsed.path("flashcardData").isMissingNode()) {
                flashcardData = objectMapper.convertValue(parsed.path("flashcardData"), Object.class);
            }
            if ("replace_ai_selections".equals(action) && parsed.path("selectionEdits").isArray()) {
                selectionEdits = objectMapper.convertValue(parsed.path("selectionEdits"), new TypeReference<>() {});
            }
            if ("replace_ai_selections".equals(action) && (selectionEdits == null || selectionEdits.isEmpty())) {
                action = "none";
            }

            return new AiResponse(
                reply.isBlank() ? aiMessage : reply, action,
                editorContent.isBlank() ? null : editorContent, conversationTitle,
                selectionEdits, quizData, flashcardData
            );
        } catch (Exception e) {
            String reply = extractFieldFallback(trimmed, "reply");
            String action = extractFieldFallback(trimmed, "action");
            String editorContent = extractFieldFallback(trimmed, "editorContent");
            String conversationTitle = extractFieldFallback(trimmed, "conversationTitle");
            if (!VALID_ACTIONS.contains(action)) action = "none";

            return new AiResponse(
                reply.isBlank() ? aiMessage : reply, action,
                editorContent.isBlank() ? null : editorContent, conversationTitle,
                null, null, null
            );
        }
    }

    private List<AiSelectionTarget> normalizeAiSelections(List<AiSelectionTarget> rawTargets) {
        if (rawTargets == null || rawTargets.isEmpty()) return List.of();
        return rawTargets.stream()
            .filter(t -> t != null && t.getText() != null && !t.getText().trim().isBlank())
            .map(t -> {
                AiSelectionTarget n = new AiSelectionTarget();
                n.setId(t.getId() == null || t.getId().isBlank() ? UUID.randomUUID().toString() : t.getId().trim());
                n.setText(t.getText().trim());
                return n;
            }).toList();
    }

    private String sanitizeConversationTitle(String value, String fallbackSource) {
        String cleaned = value != null ? value.replaceAll("[\\r\\n]+", " ").replace("\"", "").trim() : "";
        if (cleaned.isBlank()) cleaned = buildFallbackConversationTitle(fallbackSource);
        if (cleaned.length() > 60) cleaned = cleaned.substring(0, 60).trim();
        return cleaned.isBlank() ? "New chat" : cleaned;
    }

    private String buildFallbackConversationTitle(String fallbackSource) {
        if (fallbackSource == null || fallbackSource.isBlank()) return "New chat";
        String normalized = fallbackSource.replaceAll("[\\r\\n]+", " ").replaceAll("[^\\p{L}\\p{N}\\s]", " ").replaceAll("\\s+", " ").trim();
        if (normalized.isBlank()) return "New chat";
        String[] words = normalized.split(" ");
        return String.join(" ", List.of(words).subList(0, Math.min(words.length, 6)));
    }

    private String extractFieldFallback(String json, String field) {
        try {
            String key = "\"" + field + "\"";
            int keyIdx = json.indexOf(key);
            if (keyIdx == -1) return "";
            int colonIdx = json.indexOf(':', keyIdx + key.length());
            if (colonIdx == -1) return "";
            int quoteStart = json.indexOf('"', colonIdx + 1);
            if (quoteStart == -1) return "";

            StringBuilder sb = new StringBuilder();
            int i = quoteStart + 1;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    switch (next) {
                        case '"'  -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case 'n'  -> sb.append('\n');
                        case 't'  -> sb.append('\t');
                        case 'r'  -> sb.append('\r');
                        default   -> sb.append(next);
                    }
                    i += 2;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                    i++;
                }
            }
            return sb.toString();
        } catch (Exception ignored) {}
        return "";
    }
}
