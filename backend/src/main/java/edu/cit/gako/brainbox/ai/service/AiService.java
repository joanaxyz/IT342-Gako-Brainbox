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

    private static final Set<String> VALID_ACTIONS = Set.of(
        "none", "add_to_editor", "replace_editor", "replace_selection", "replace_ai_selections", "create_quiz", "create_flashcard"
    );
    private static final Set<String> QUIZ_ACTION_ALIASES = Set.of(
        "create_quiz", "create_quizzes", "generate_quiz", "generate_quizzes", "quiz", "quizzes"
    );
    private static final Set<String> FLASHCARD_ACTION_ALIASES = Set.of(
        "create_flashcard", "create_flashcards", "generate_flashcard", "generate_flashcards",
        "create_flashcard_deck", "create_deck", "deck", "flashcard", "flashcards"
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
            String action = normalizeAction(parsed.path("action").asText("none"));
            String editorContent = parsed.path("editorContent").asText("");
            String conversationTitle = parsed.path("conversationTitle").asText("");

            if (reply.isBlank()) reply = extractFieldFallback(trimmed, "reply");
            if (conversationTitle.isBlank()) conversationTitle = extractFieldFallback(trimmed, "conversationTitle");

            Object quizData = extractQuizData(parsed);
            Object flashcardData = extractFlashcardData(parsed);
            List<AiSelectionEdit> selectionEdits = null;
            if ("replace_ai_selections".equals(action) && parsed.path("selectionEdits").isArray()) {
                selectionEdits = objectMapper.convertValue(parsed.path("selectionEdits"), new TypeReference<>() {});
            }
            if ("replace_ai_selections".equals(action) && (selectionEdits == null || selectionEdits.isEmpty())) {
                action = "none";
            }
            if ("create_quiz".equals(action) && quizData == null) {
                action = flashcardData != null ? "create_flashcard" : "none";
            }
            if ("create_flashcard".equals(action) && flashcardData == null) {
                action = quizData != null ? "create_quiz" : "none";
            }
            if ("none".equals(action)) {
                if (quizData != null && flashcardData == null) {
                    action = "create_quiz";
                } else if (flashcardData != null && quizData == null) {
                    action = "create_flashcard";
                }
            }

            return new AiResponse(
                reply.isBlank() ? aiMessage : reply, action,
                editorContent.isBlank() ? null : editorContent, conversationTitle,
                selectionEdits, quizData, flashcardData
            );
        } catch (Exception e) {
            String reply = extractFieldFallback(trimmed, "reply");
            String action = normalizeAction(extractFieldFallback(trimmed, "action"));
            String editorContent = extractFieldFallback(trimmed, "editorContent");
            String conversationTitle = extractFieldFallback(trimmed, "conversationTitle");

            return new AiResponse(
                reply.isBlank() ? aiMessage : reply, action,
                editorContent.isBlank() ? null : editorContent, conversationTitle,
                null, null, null
            );
        }
    }

    private String normalizeAction(String rawAction) {
        String normalizedAction = rawAction != null ? rawAction.trim().toLowerCase() : "";

        if (QUIZ_ACTION_ALIASES.contains(normalizedAction)) {
            return "create_quiz";
        }
        if (FLASHCARD_ACTION_ALIASES.contains(normalizedAction)) {
            return "create_flashcard";
        }

        return VALID_ACTIONS.contains(normalizedAction) ? normalizedAction : "none";
    }

    private Object extractQuizData(JsonNode parsed) {
        List<JsonNode> candidates = Arrays.asList(
            parsed.get("quizData"),
            parsed.get("quiz"),
            parsed.get("quiz_data"),
            parsed.get("generatedQuiz"),
            buildQuizDataFromTopLevel(parsed)
        );

        for (JsonNode candidate : candidates) {
            JsonNode normalizedCandidate = normalizePayloadNode(candidate);
            if (normalizedCandidate == null || !normalizedCandidate.isObject()) {
                continue;
            }

            JsonNode questionsNode = normalizedCandidate.path("questions");
            if (!questionsNode.isArray() || questionsNode.isEmpty()) {
                continue;
            }

            return objectMapper.convertValue(normalizedCandidate, Object.class);
        }

        return null;
    }

    private Object extractFlashcardData(JsonNode parsed) {
        List<JsonNode> candidates = Arrays.asList(
            parsed.get("flashcardData"),
            parsed.get("flashcardsData"),
            parsed.get("flashcardDeck"),
            parsed.get("deckData"),
            parsed.get("deck"),
            parsed.get("generatedDeck"),
            parsed.get("generatedFlashcards"),
            buildFlashcardDataFromTopLevel(parsed)
        );

        for (JsonNode candidate : candidates) {
            JsonNode normalizedCandidate = normalizePayloadNode(candidate);
            JsonNode flashcardNode = normalizeFlashcardPayload(normalizedCandidate);

            if (flashcardNode == null || !flashcardNode.isObject()) {
                continue;
            }

            JsonNode cardsNode = flashcardNode.path("cards");
            if (!cardsNode.isArray() || cardsNode.isEmpty()) {
                continue;
            }

            return objectMapper.convertValue(flashcardNode, Object.class);
        }

        return null;
    }

    private JsonNode buildQuizDataFromTopLevel(JsonNode parsed) {
        if (!parsed.path("questions").isArray() || parsed.path("questions").isEmpty()) {
            return null;
        }

        var quizNode = objectMapper.createObjectNode();
        copyTextField(parsed, quizNode, "title");
        copyTextField(parsed, quizNode, "description");
        copyTextField(parsed, quizNode, "difficulty");
        quizNode.set("questions", parsed.path("questions"));
        return quizNode;
    }

    private JsonNode buildFlashcardDataFromTopLevel(JsonNode parsed) {
        JsonNode cardsNode = null;
        if (parsed.path("cards").isArray() && !parsed.path("cards").isEmpty()) {
            cardsNode = parsed.path("cards");
        } else if (parsed.path("flashcards").isArray() && !parsed.path("flashcards").isEmpty()) {
            cardsNode = parsed.path("flashcards");
        }

        if (cardsNode == null) {
            return null;
        }

        var flashcardNode = objectMapper.createObjectNode();
        copyTextField(parsed, flashcardNode, "title");
        copyTextField(parsed, flashcardNode, "description");
        flashcardNode.set("cards", cardsNode);
        return flashcardNode;
    }

    private JsonNode normalizePayloadNode(JsonNode candidate) {
        if (candidate == null || candidate.isNull() || candidate.isMissingNode()) {
            return null;
        }

        if (candidate.isTextual()) {
            String rawJson = candidate.asText("").trim();
            if (rawJson.isBlank()) {
                return null;
            }

            try {
                return objectMapper.readTree(rawJson);
            } catch (Exception ignored) {
                return null;
            }
        }

        return candidate;
    }

    private JsonNode normalizeFlashcardPayload(JsonNode candidate) {
        JsonNode normalizedCandidate = normalizePayloadNode(candidate);
        if (normalizedCandidate == null) {
            return null;
        }

        if (normalizedCandidate.isArray()) {
            var flashcardNode = objectMapper.createObjectNode();
            flashcardNode.set("cards", normalizedCandidate);
            return flashcardNode;
        }

        if (!normalizedCandidate.isObject()) {
            return null;
        }

        if (normalizedCandidate.path("cards").isArray()) {
            return normalizedCandidate;
        }

        if (normalizedCandidate.path("flashcards").isArray()) {
            var flashcardNode = objectMapper.createObjectNode();
            copyTextField(normalizedCandidate, flashcardNode, "title");
            copyTextField(normalizedCandidate, flashcardNode, "description");
            flashcardNode.set("cards", normalizedCandidate.path("flashcards"));
            return flashcardNode;
        }

        return null;
    }

    private void copyTextField(JsonNode source, com.fasterxml.jackson.databind.node.ObjectNode target, String fieldName) {
        if (source == null || target == null || fieldName == null || fieldName.isBlank()) {
            return;
        }

        String value = source.path(fieldName).asText("");
        if (!value.isBlank()) {
            target.put(fieldName, value);
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
