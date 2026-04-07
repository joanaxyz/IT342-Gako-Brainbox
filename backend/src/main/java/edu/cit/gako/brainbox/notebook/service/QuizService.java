package edu.cit.gako.brainbox.notebook.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.gako.brainbox.auth.service.UserService;
import edu.cit.gako.brainbox.notebook.dto.request.QuizAttemptRequest;
import edu.cit.gako.brainbox.notebook.dto.request.QuizQuestionRequest;
import edu.cit.gako.brainbox.notebook.dto.request.QuizRequest;
import edu.cit.gako.brainbox.notebook.dto.response.QuizQuestionResponse;
import edu.cit.gako.brainbox.notebook.dto.response.QuizResponse;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.entity.Quiz;
import edu.cit.gako.brainbox.notebook.entity.QuizAttempt;
import edu.cit.gako.brainbox.notebook.entity.QuizQuestion;
import edu.cit.gako.brainbox.notebook.repository.QuizAttemptRepository;
import edu.cit.gako.brainbox.notebook.repository.QuizRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final NotebookService notebookService;
    private final UserService userService;

    @Transactional
    public QuizResponse createQuiz(QuizRequest request, Long userId) {
        Quiz quiz = new Quiz();
        quiz.setUser(userService.findById(userId));
        applyRequest(quiz, request, userId);
        return mapToResponse(quizRepository.save(quiz));
    }

    public List<QuizResponse> getQuizzesByUser(Long userId) {
        return quizRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public QuizResponse getQuizResponse(String uuid, Long userId) {
        return mapToResponse(getQuizByUuidAndUserId(uuid, userId));
    }

    @Transactional
    public QuizResponse updateQuiz(String uuid, Long userId, QuizRequest request) {
        Quiz quiz = getQuizByUuidAndUserId(uuid, userId);
        applyRequest(quiz, request, userId);
        return mapToResponse(quizRepository.save(quiz));
    }

    @Transactional
    public void deleteQuiz(String uuid, Long userId) {
        Quiz quiz = getQuizByUuidAndUserId(uuid, userId);
        quizAttemptRepository.deleteByQuizId(quiz.getId());
        quizRepository.delete(quiz);
    }

    @Transactional
    public QuizResponse recordAttempt(String uuid, Long userId, QuizAttemptRequest request) {
        Quiz quiz = getQuizByUuidAndUserId(uuid, userId);
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(userService.findById(userId));
        attempt.setScore(request.getScore());
        quizAttemptRepository.save(attempt);
        return mapToResponse(quiz);
    }

    private void applyRequest(Quiz quiz, QuizRequest request, Long userId) {
        if (request.getTitle() != null) quiz.setTitle(request.getTitle());
        if (request.getDescription() != null) quiz.setDescription(request.getDescription());
        if (request.getDifficulty() != null) quiz.setDifficulty(request.getDifficulty());

        if (request.getNotebookUuid() != null && !request.getNotebookUuid().isBlank()) {
            Notebook notebook = notebookService.getNotebookByUuid(request.getNotebookUuid());
            notebook.assertOwnedBy(userId);
            quiz.setNotebook(notebook);
        } else {
            quiz.setNotebook(null);
        }

        if (request.getQuestions() != null) {
            quiz.getQuestions().clear();
            for (QuizQuestionRequest qr : request.getQuestions()) {
                QuizQuestion question = new QuizQuestion();
                question.setType(qr.getType());
                question.setText(qr.getText());
                question.setOptions(qr.getOptions());
                question.setCorrectIndex(qr.getCorrectIndex());
                quiz.getQuestions().add(question);
            }
        }
    }

    private Quiz getQuizByUuid(String uuid) {
        return quizRepository.findByUuid(uuid)
                .orElseThrow(() -> new NoSuchElementException("Quiz not found"));
    }

    private Quiz getQuizByUuidAndUserId(String uuid, Long userId) {
        Quiz quiz = getQuizByUuid(uuid);
        quiz.assertOwnedBy(userId);
        return quiz;
    }

    private QuizResponse mapToResponse(Quiz quiz) {
        QuizResponse response = new QuizResponse();
        response.setUuid(quiz.getUuid());
        response.setTitle(quiz.getTitle());
        response.setDescription(quiz.getDescription());
        response.setDifficulty(quiz.getDifficulty());
        response.setCreatedAt(quiz.getCreatedAt());
        response.setUpdatedAt(quiz.getUpdatedAt());

        if (quiz.getNotebook() != null) {
            response.setNotebookUuid(quiz.getNotebook().getUuid());
            response.setNotebookTitle(quiz.getNotebook().getTitle());
        }

        List<QuizQuestionResponse> questions = quiz.getQuestions().stream().map(q -> {
            QuizQuestionResponse qr = new QuizQuestionResponse();
            qr.setType(q.getType());
            qr.setText(q.getText());
            qr.setOptions(q.getOptions());
            qr.setCorrectIndex(q.getCorrectIndex());
            return qr;
        }).toList();

        response.setQuestions(questions);
        response.setQuestionCount(questions.size());
        response.setEstimatedTime(Math.max(1, questions.size() * 2) + " min");

        long attemptCount = quizAttemptRepository.countByQuizId(quiz.getId());
        response.setAttempts(attemptCount);
        response.setBestScore(quizAttemptRepository.findBestScoreByQuizId(quiz.getId()).orElse(null));

        return response;
    }
}
