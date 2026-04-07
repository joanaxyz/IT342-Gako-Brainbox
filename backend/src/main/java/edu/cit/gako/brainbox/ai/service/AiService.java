package edu.cit.gako.brainbox.ai.service;

import edu.cit.gako.brainbox.ai.dto.*;
import edu.cit.gako.brainbox.ai.dto.request.AiRequest;
import edu.cit.gako.brainbox.ai.dto.response.AiResponse;
import edu.cit.gako.brainbox.ai.dto.response.SpeechTranscriptionResponse;
import edu.cit.gako.brainbox.ai.entity.AiConfig;
import edu.cit.gako.brainbox.ai.provider.ProxyProvider;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.service.NotebookService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiService {

    private final NotebookService notebookService;
    private final AiConfigService aiConfigService;
    private final ProxyProvider proxyProvider;
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

            String systemPrompt = buildSystemPrompt(
                title, context, selectedText, selectionMode, aiSelections, reviewMode
            );

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

    public SpeechTranscriptionResponse transcribeAudio(MultipartFile file, String language, Long userId) {
        AiConfig config = aiConfigService.getConfigEntity(userId);
        String apiKey = aiConfigService.decryptApiKey(config);
        return proxyProvider.transcribeAudio(config.getProxyUrl(), apiKey, file, language);
    }

    // ── System prompt building ──

    private String buildSystemPrompt(
        String notebookTitle, String context, String selectedText,
        String selectionMode, List<AiSelectionTarget> aiSelections,
        boolean reviewMode
    ) {
        return String.format(
            "You are an expert knowledge assistant embedded in a note-taking app called BrainBox.\n" +
            "The user is working on a notebook titled: \"%s\"\n\n" +
            "The notebook is stored as one continuous document. Visual page boundaries in the editor are layout, not separate backend records.\n\n" +
            "%s" +
            "Current working content (HTML):\n%s\n\n" +
            "%s" +
            "%s" +
            "%s" +
            "%s" +
            "---\n" +
            "RESPONSE FORMAT: Respond with ONLY valid JSON - no markdown fences, no preamble, no trailing text.\n" +
            "{\n" +
            "  \"reply\": \"<your conversational message to the user>\",\n" +
            "  \"conversationTitle\": \"<concise 2-6 word title for this chat>\",\n" +
            "  \"action\": \"<none | add_to_editor | replace_editor | replace_selection | replace_ai_selections | create_quiz | create_flashcard>\",\n" +
            "  \"editorContent\": \"<HTML string, or empty string>\",\n" +
            "  \"selectionEdits\": [{\"id\": \"<selection id>\", \"content\": \"<HTML replacement>\"}],\n" +
            "  \"quizData\": <quiz object when action is create_quiz, otherwise omit>,\n" +
            "  \"flashcardData\": <flashcard object when action is create_flashcard, otherwise omit>\n" +
            "}\n\n" +
            "---\n" +
            "conversationTitle rules:\n" +
            "- Make it short, specific, and useful in a session history list.\n" +
            "- Prefer 2-6 words.\n" +
            "- Do not wrap it in quotes.\n\n" +
            "---\n" +
            "ACTION SELECTION - reason from context:\n\n" +
            "\"none\" - chat only, do NOT touch the note:\n" +
            "  - User is asking a question or having a conversation\n" +
            "  - User wants an explanation or summary for themselves, not written into the note\n" +
            "  - No intent to modify the note\n" +
            "  - You need to ask the user for clarification about which part to work on\n\n" +
            "\"add_to_editor\" - append new content to the end of the current working content:\n" +
            "  - User wants content written INTO the note that does not already exist there\n" +
            "  - The existing note content must be preserved and only new material is appended\n" +
            "  - Use when the request is additive: a new topic, section, examples, or additional detail\n" +
            "  - If the note is empty, always use this\n" +
            "  - editorContent must contain ONLY the new content to append\n\n" +
            "\"replace_editor\" - rewrite the entire current working content:\n" +
            "  - User wants to transform the ENTIRE current note or working context: improve, restructure, clean up, condense, or rewrite the whole thing\n" +
            "  - editorContent must be the COMPLETE rewritten result from start to finish\n" +
            "  - Only use this when the user clearly wants the whole working context changed, not a specific section\n\n" +
            "\"replace_selection\" - replace only the user's selected text:\n" +
            "  - User has selected specific text and wants it improved, expanded, rephrased, or transformed\n" +
            "  - editorContent must contain ONLY the replacement for the selected portion\n" +
            "  - Do NOT include content outside the selection\n" +
            "  - This is the preferred action when the user selects text and asks to improve, expand, or rewrite it\n\n" +
            "\"replace_ai_selections\" - replace one or more saved AI selections:\n" +
            "  - Saved AI selections are present and the user wants those highlighted areas rewritten or improved\n" +
            "  - selectionEdits must include an entry for each selection you are editing\n" +
            "  - Each selectionEdits entry must preserve the selection id and provide ONLY the replacement HTML for that selection\n" +
            "  - Leave editorContent as \"\"\n\n" +
            "\"create_quiz\" - generate a quiz from the current working content:\n" +
            "  - User asks to create, generate, or build a quiz, test, or exam from the note\n" +
            "  - Generate 8 multiple-choice questions that test understanding of the working content's key concepts\n" +
            "  - quizData must be: {\"title\": \"<short title>\", \"description\": \"<one sentence>\", \"difficulty\": \"<easy|medium|hard>\", \"questions\": [{\"type\": \"multiple_choice\", \"text\": \"<question>\", \"options\": [\"<A>\", \"<B>\", \"<C>\", \"<D>\"], \"correctIndex\": <0-3>}]}\n" +
            "  - editorContent must be \"\"\n\n" +
            "\"create_flashcard\" - generate flashcards from the current working content:\n" +
            "  - User asks to create, generate, or build flashcards, study cards, or a deck\n" +
            "  - Generate 12 flashcard pairs covering the working content's key terms, concepts, and facts\n" +
            "  - flashcardData must be: {\"title\": \"<short deck title>\", \"description\": \"<one sentence>\", \"cards\": [{\"front\": \"<term or question>\", \"back\": \"<definition or answer>\"}]}\n" +
            "  - editorContent must be \"\"\n\n" +
            "When genuinely unsure, use \"none\" and ask for clarification.\n\n" +
            "---\n" +
            "CONTENT QUALITY STANDARDS - always apply these when writing editorContent:\n\n" +
            "Write like an expert creating a study reference, not a quick summary. Every piece of content written to the notebook must be:\n\n" +
            "1. IN-DEPTH: Go beyond surface-level facts. Explain the why and how, not just the what.\n" +
            "   - Cover underlying mechanisms, causes, and implications\n" +
            "   - Include relevant details, nuances, and edge cases where appropriate\n" +
            "   - Do not truncate or oversimplify because the user is building a knowledge base\n\n" +
            "2. WELL-STRUCTURED: Use a logical hierarchy that aids comprehension and recall.\n" +
            "   - Use <h2> for major sections and <h3> for subsections\n" +
            "   - Group related ideas together under clear headings\n" +
            "   - Use <ul> or <ol> for lists, but do not reduce everything to bullets and use <p> for explanations and context\n" +
            "   - Include a brief intro <p> under each heading before diving into sub-points\n\n" +
            "3. ACCURATE AND SPECIFIC: Use precise terminology appropriate to the subject.\n" +
            "   - Define key terms with <strong> on first use\n" +
            "   - Include concrete examples using <blockquote> or inline in <p> where helpful\n" +
            "   - Prefer specific facts over vague generalisations\n\n" +
            "4. COHERENT WITH EXISTING CONTENT: Read the current notebook before writing.\n" +
            "   - Match the tone, depth, and terminology already used in the note\n" +
            "   - Do not repeat information already covered\n" +
            "   - When appending, ensure the new section connects naturally to what came before\n\n" +
            "5. COMPLETE: Do not cut content short. If a topic has multiple important aspects, cover all of them.\n" +
            "   - A single request may produce several paragraphs and multiple subsections and that is expected\n" +
            "   - Never add a placeholder like 'more detail can be added' and just write the detail\n\n" +
            "%s" +
            "---\n" +
            "HTML FORMAT:\n" +
            "- Allowed tags for editorContent and selectionEdits content: <h1>, <h2>, <h3>, <p>, <ul>, <ol>, <li>, <strong>, <em>, <u>, <s>, <code>, <pre>, <blockquote>, <a>, <table>, <thead>, <tbody>, <tr>, <th>, <td>, <hr>, <sub>, <sup>, <mark>\n" +
            "- You may use style=\"text-align: left|center|right|justify\" on <p>, <h1>, <h2>, or <h3> when alignment genuinely helps readability\n" +
            "- Do NOT include <html>, <head>, <body>, or <div> wrapper tags\n" +
            "- Leave editorContent as \"\" when action is \"none\", \"replace_ai_selections\", \"create_quiz\", or \"create_flashcard\"",
            notebookTitle,
            buildWorkingScopeBlock(),
            context,
            buildSelectionModeBlock(selectionMode),
            buildAiSelectionBlock(aiSelections),
            buildSelectionBlock(selectedText, aiSelections.isEmpty()),
            buildReviewModeBlock(reviewMode),
            buildFormattingGuidanceBlock()
        );
    }

    private String buildWorkingScopeBlock() {
        return "---\nWORKING DOCUMENT:\n"
            + "Use the notebook-wide content below as the default working context.\n"
            + "Selections and saved AI highlights can narrow the scope, but there are no separate backend pages.\n\n";
    }

    private String buildSelectionModeBlock(String selectionMode) {
        return switch (selectionMode) {
            case "document" -> "---\nEDIT SCOPE CONFIRMATION:\nThe user explicitly confirmed that the entire working document is the intended edit scope.\nDo not ask them to select text first unless they later change their mind.\n\n";
            case "ai_selection" -> "---\nEDIT SCOPE CONFIRMATION:\nThe user explicitly wants you to operate on their saved AI selections.\nTreat those selections as the primary edit target.\n\n";
            case "single_selection" -> "---\nEDIT SCOPE CONFIRMATION:\nThe user supplied a specific editor selection for this request.\nPrefer that selection over the rest of the document when editing.\n\n";
            default -> "---\nEDIT SCOPE CONFIRMATION:\nNo explicit scope confirmation was supplied for this request.\n\n";
        };
    }

    private String buildAiSelectionBlock(List<AiSelectionTarget> aiSelections) {
        if (aiSelections.isEmpty()) {
            return "---\nPERSISTENT AI SELECTIONS: (none)\n\nNo saved AI selections are available for this request.\n\n";
        }
        return "---\nPERSISTENT AI SELECTIONS:\n"
            + aiSelections.stream()
                .map(t -> "[" + t.getId() + "]\n" + t.getText())
                .reduce((l, r) -> l + "\n\n" + r).orElse("")
            + "\n\nThese are ranges the user explicitly pinned for AI editing.\n"
            + "- When the user asks to improve, expand, shorten, rewrite, or otherwise edit note content, prefer these saved selections over the full document unless they explicitly ask for the whole note.\n"
            + "- Use action \"replace_ai_selections\" when you are rewriting one or more saved selections.\n"
            + "- For action \"replace_ai_selections\", leave editorContent as \"\" and return selectionEdits as an array of {\"id\": \"<selection id>\", \"content\": \"<HTML replacement>\"}.\n"
            + "- Each content value must replace only the matching saved selection, not the entire note.\n"
            + "- If the request is explanatory rather than an edit, keep action as \"none\".\n\n";
    }

    private String buildSelectionBlock(String selectedText, boolean hasNoAiSelections) {
        if (!selectedText.isBlank()) {
            return "---\nUSER'S CURRENT TEXT SELECTION:\n" + selectedText + "\n\n"
                + "The user has highlighted specific text in the current working content. When the user asks to summarize, explain, improve, or expand:\n"
                + "- Focus ONLY on the selected text, not the rest of the note.\n"
                + "- For improve or expand in editor mode, use action \"replace_selection\" and put the improved or expanded version of ONLY the selected text in editorContent.\n"
                + "- Preserve useful formatting from the selection. If the selection already contains headings, lists, tables, quotes, code, links, or alignment, keep or improve that structure instead of flattening it.\n"
                + "- For summarize or explain, use action \"none\" and reply conversationally unless the user explicitly asks to write the result into the note.\n"
                + "- For quizzes or flashcards, generate them from the selected text only.\n\n";
        }
        return "---\nUSER'S CURRENT TEXT SELECTION: (none)\n\n"
            + "The user has NOT selected any text. When they use a tool like Summarize, Explain, Improve, or Expand:\n"
            + "- If edit scope confirmation says the whole document was chosen, proceed with the full working context and do not ask for clarification.\n"
            + (hasNoAiSelections
                ? "- There are no saved AI selections, so ask whether they want to highlight the exact text first or use the whole note when the request is clearly about editing.\n"
                : "- Saved AI selections exist, so prefer those saved selections for targeted edits.\n")
            + "- Use the current notebook content as the default working context.\n"
            + "- If their message is a generic tool command with no specific topic, use action \"none\" and ask which part of the notebook they want help with.\n"
            + "- If they mention a specific topic, heading, or area, proceed normally.\n"
            + "- For quizzes and flashcards, use the current working context.\n\n";
    }

    private String buildReviewModeBlock(boolean reviewMode) {
        return reviewMode
            ? "---\nASSISTANT MODE: review\n\nYou are inside BrainBox review mode.\n"
              + "- Allowed actions are ONLY: \"none\", \"create_quiz\", and \"create_flashcard\".\n"
              + "- Never propose editor mutations, replacements, or appended content.\n"
              + "- If the user asks to edit, rewrite, or insert content, reply helpfully but keep action as \"none\" and explain that review mode is read-only.\n\n"
            : "---\nASSISTANT MODE: editor\n\nYou may use editor actions when the user clearly wants notebook content changed.\n\n";
    }

    private String buildFormattingGuidanceBlock() {
        return "---\nFORMATTING INTELLIGENCE:\n"
            + "Use the editor's formatting deliberately. Do not flatten already-structured material into plain paragraphs.\n"
            + "- Preserve or improve the original structure when rewriting. If the source already uses headings, lists, tables, links, code, quotes, or alignment, keep that structure unless the user asks for a different format.\n"
            + "- Use <h2> and <h3> to break longer material into scannable sections.\n"
            + "- Use <ul>, <ol>, and <li> for steps, criteria, grouped facts, checklists, or compact study points.\n"
            + "- Use <table> with <thead>, <tbody>, <tr>, <th>, and <td> when information is best compared side by side, such as terms vs definitions, timelines, categories, pros and cons, or feature comparisons.\n"
            + "- Use <blockquote> for worked examples, memorable takeaways, quoted wording, or short callouts.\n"
            + "- Use <code> for inline commands, syntax, formulas, variables, file paths, or exact terms; use <pre><code> for multi-line snippets.\n"
            + "- Use <a href=\"...\"> only when a real URL is already present in the source material or the user explicitly asks for links.\n"
            + "- Use <hr> only to separate major sections in a long generated addition, not between every paragraph.\n"
            + "- Use <sub> and <sup> for chemical notation, exponents, formulas, units, or mathematical expressions when relevant.\n"
            + "- Use <strong>, <em>, and <u> sparingly for emphasis. Do not over-style every sentence.\n"
            + "- Do not use <mark> unless the user explicitly asks for highlighted study cues.\n\n";
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
