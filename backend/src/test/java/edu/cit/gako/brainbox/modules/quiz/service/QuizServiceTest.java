package edu.cit.gako.brainbox.modules.quiz.service;

import edu.cit.gako.brainbox.modules.notebook.service.NotebookService;
import edu.cit.gako.brainbox.modules.notebook.entity.Notebook;
import edu.cit.gako.brainbox.modules.quiz.dto.request.QuizAttemptRequest;
import edu.cit.gako.brainbox.modules.quiz.dto.request.QuizQuestionRequest;
import edu.cit.gako.brainbox.modules.quiz.dto.request.QuizRequest;
import edu.cit.gako.brainbox.modules.quiz.entity.Quiz;
import edu.cit.gako.brainbox.modules.quiz.repository.QuizAttemptRepository;
import edu.cit.gako.brainbox.modules.quiz.repository.QuizRepository;
import edu.cit.gako.brainbox.modules.user.service.UserService;
import edu.cit.gako.brainbox.modules.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private NotebookService notebookService;

    @Mock
    private UserService userService;

    @InjectMocks
    private QuizService quizService;

    @Test
    void createQuizPersistsQuestionsInsideAggregate() {
        User user = new User();
        user.setId(11L);

        User owner = new User();
        owner.setId(11L);

        Notebook notebook = new Notebook();
        notebook.setUuid("nb-1");
        notebook.setUser(owner);

        QuizQuestionRequest question = new QuizQuestionRequest();
        question.setType("multiple-choice");
        question.setText("What?");
        question.setOptions(List.of("A", "B"));
        question.setCorrectIndex(0);

        QuizRequest request = new QuizRequest();
        request.setTitle("Quiz");
        request.setNotebookUuid("nb-1");
        request.setQuestions(List.of(question));

        when(userService.findById(11L)).thenReturn(user);
        when(notebookService.getNotebookByUuid("nb-1")).thenReturn(notebook);
        when(quizRepository.save(any(Quiz.class))).thenAnswer((invocation) -> {
            Quiz quiz = invocation.getArgument(0);
            quiz.setId(90L);
            return quiz;
        });
        when(quizAttemptRepository.countByQuizId(90L)).thenReturn(0L);
        when(quizAttemptRepository.findBestScoreByQuizId(90L)).thenReturn(Optional.empty());

        quizService.createQuiz(request, 11L);

        ArgumentCaptor<Quiz> captor = ArgumentCaptor.forClass(Quiz.class);
        verify(quizRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getQuestions().size());
        assertEquals("multiple-choice", captor.getValue().getQuestions().get(0).getType());
        assertEquals(notebook, captor.getValue().getNotebook());
        assertEquals(user, captor.getValue().getUser());
    }

    @Test
    void recordAttemptPersistsAttemptThroughQuizService() {
        User user = new User();
        user.setId(11L);

        Quiz quiz = new Quiz();
        quiz.setId(5L);
        quiz.setUuid("quiz-1");
        quiz.setUser(user);
        quiz.setQuestions(List.of());

        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setScore(88);
        request.setClientMutationId("attempt-1");

        when(quizRepository.findByUuid("quiz-1")).thenReturn(Optional.of(quiz));
        when(userService.findById(11L)).thenReturn(user);
        when(quizAttemptRepository.findByUserIdAndClientMutationId(11L, "attempt-1")).thenReturn(Optional.empty());
        when(quizAttemptRepository.countByQuizId(5L)).thenReturn(1L);
        when(quizAttemptRepository.findBestScoreByQuizId(5L)).thenReturn(Optional.of(88));

        quizService.recordAttempt("quiz-1", 11L, request);

        verify(quizAttemptRepository).save(any());
    }
}
