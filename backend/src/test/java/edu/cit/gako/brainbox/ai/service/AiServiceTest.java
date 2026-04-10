package edu.cit.gako.brainbox.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cit.gako.brainbox.ai.dto.request.AiRequest;
import edu.cit.gako.brainbox.ai.dto.response.AiResponse;
import edu.cit.gako.brainbox.ai.entity.AiConfig;
import edu.cit.gako.brainbox.ai.provider.AiProvider;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.service.NotebookService;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private NotebookService notebookService;

    @Mock
    private AiConfigService aiConfigService;

    @Mock
    private AiProvider aiProvider;

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService(notebookService, aiConfigService, aiProvider, new ObjectMapper());
    }

    @Test
    void generateResponseBuildsQuizDataFromTopLevelQuestions() {
        when(aiConfigService.getConfigEntity(7L)).thenReturn(createConfig());
        when(aiConfigService.decryptApiKey(any(AiConfig.class))).thenReturn("secret");
        when(notebookService.getNotebookByUuid("nb-quiz")).thenReturn(createNotebook("nb-quiz"));
        when(aiProvider.generateResponse(anyString(), anyString(), anyString(), anyList(), anyDouble()))
            .thenReturn("""
                {
                  "reply": "I drafted a quiz for this note.",
                  "action": "quiz",
                  "title": "Biology Quiz",
                  "description": "Checks the core ideas from the note.",
                  "difficulty": "medium",
                  "questions": [
                    {
                      "text": "What is the powerhouse of the cell?",
                      "options": ["Nucleus", "Mitochondrion", "Ribosome", "Golgi body"],
                      "correctIndex": 1
                    }
                  ]
                }
                """);

        AiResponse response = aiService.generateResponse(createRequest("nb-quiz"), 7L);

        assertEquals("create_quiz", response.getAction());
        assertNotNull(response.getQuizData());
        Map<?, ?> quizData = assertInstanceOf(Map.class, response.getQuizData());
        assertEquals("Biology Quiz", quizData.get("title"));
        assertEquals(1, ((List<?>) quizData.get("questions")).size());
    }

    @Test
    void generateResponseBuildsFlashcardDataFromFlashcardsAlias() {
        when(aiConfigService.getConfigEntity(9L)).thenReturn(createConfig());
        when(aiConfigService.decryptApiKey(any(AiConfig.class))).thenReturn("secret");
        when(notebookService.getNotebookByUuid("nb-deck")).thenReturn(createNotebook("nb-deck"));
        when(aiProvider.generateResponse(anyString(), anyString(), anyString(), anyList(), anyDouble()))
            .thenReturn("""
                {
                  "reply": "Here is a flashcard deck.",
                  "action": "flashcards",
                  "title": "Physics Deck",
                  "description": "Flashcards for the note's main ideas.",
                  "flashcards": [
                    {
                      "front": "Velocity",
                      "back": "Speed with direction."
                    }
                  ]
                }
                """);

        AiResponse response = aiService.generateResponse(createRequest("nb-deck"), 9L);

        assertEquals("create_flashcard", response.getAction());
        assertNotNull(response.getFlashcardData());
        Map<?, ?> flashcardData = assertInstanceOf(Map.class, response.getFlashcardData());
        assertEquals("Physics Deck", flashcardData.get("title"));
        assertEquals(1, ((List<?>) flashcardData.get("cards")).size());
    }

    private AiRequest createRequest(String notebookUuid) {
        AiRequest request = new AiRequest();
        request.setNotebookUuid(notebookUuid);
        request.setQuery("Generate study material.");
        request.setMode("review");
        return request;
    }

    private AiConfig createConfig() {
        AiConfig config = new AiConfig();
        config.setName("Test config");
        config.setModel("gpt-test");
        config.setProxyUrl("https://example.com");
        config.setApiKey("encrypted");
        return config;
    }

    private Notebook createNotebook(String uuid) {
        Notebook notebook = new Notebook();
        notebook.setUuid(uuid);
        notebook.setTitle("Study Note");
        notebook.setContent("<p>Sample study content</p>");
        return notebook;
    }
}
