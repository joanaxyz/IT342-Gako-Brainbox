package edu.cit.gako.brainbox.notebook.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.gako.brainbox.auth.service.UserService;
import edu.cit.gako.brainbox.notebook.dto.request.FlashcardAttemptRequest;
import edu.cit.gako.brainbox.notebook.dto.request.FlashcardCardRequest;
import edu.cit.gako.brainbox.notebook.dto.request.FlashcardRequest;
import edu.cit.gako.brainbox.notebook.dto.response.FlashcardCardResponse;
import edu.cit.gako.brainbox.notebook.dto.response.FlashcardResponse;
import edu.cit.gako.brainbox.notebook.entity.Flashcard;
import edu.cit.gako.brainbox.notebook.entity.FlashcardAttempt;
import edu.cit.gako.brainbox.notebook.entity.FlashcardCard;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.repository.FlashcardAttemptRepository;
import edu.cit.gako.brainbox.notebook.repository.FlashcardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardAttemptRepository flashcardAttemptRepository;
    private final NotebookService notebookService;
    private final UserService userService;

    @Transactional
    public FlashcardResponse createFlashcard(FlashcardRequest request, Long userId) {
        Flashcard flashcard = new Flashcard();
        flashcard.setUser(userService.findById(userId));
        applyRequest(flashcard, request, userId);
        return mapToResponse(flashcardRepository.save(flashcard));
    }

    public List<FlashcardResponse> getFlashcardsByUser(Long userId) {
        return flashcardRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FlashcardResponse getFlashcardResponse(String uuid, Long userId) {
        return mapToResponse(getFlashcardByUuidAndUserId(uuid, userId));
    }

    @Transactional
    public FlashcardResponse updateFlashcard(String uuid, Long userId, FlashcardRequest request) {
        Flashcard flashcard = getFlashcardByUuidAndUserId(uuid, userId);
        applyRequest(flashcard, request, userId);
        return mapToResponse(flashcardRepository.save(flashcard));
    }

    @Transactional
    public void deleteFlashcard(String uuid, Long userId) {
        Flashcard flashcard = getFlashcardByUuidAndUserId(uuid, userId);
        flashcardAttemptRepository.deleteByFlashcardId(flashcard.getId());
        flashcardRepository.delete(flashcard);
    }

    @Transactional
    public FlashcardResponse recordAttempt(String uuid, Long userId, FlashcardAttemptRequest request) {
        Flashcard flashcard = getFlashcardByUuidAndUserId(uuid, userId);
        FlashcardAttempt attempt = new FlashcardAttempt();
        attempt.setFlashcard(flashcard);
        attempt.setUser(userService.findById(userId));
        attempt.setMastery(request.getMastery());
        flashcardAttemptRepository.save(attempt);
        return mapToResponse(flashcard);
    }

    private void applyRequest(Flashcard flashcard, FlashcardRequest request, Long userId) {
        if (request.getTitle() != null) flashcard.setTitle(request.getTitle());
        if (request.getDescription() != null) flashcard.setDescription(request.getDescription());

        if (request.getNotebookUuid() != null && !request.getNotebookUuid().isBlank()) {
            Notebook notebook = notebookService.getNotebookByUuid(request.getNotebookUuid());
            notebook.assertOwnedBy(userId);
            flashcard.setNotebook(notebook);
        } else {
            flashcard.setNotebook(null);
        }

        if (request.getCards() != null) {
            flashcard.getCards().clear();
            for (FlashcardCardRequest cr : request.getCards()) {
                FlashcardCard card = new FlashcardCard();
                card.setFront(cr.getFront());
                card.setBack(cr.getBack());
                flashcard.getCards().add(card);
            }
        }
    }

    private Flashcard getFlashcardByUuid(String uuid) {
        return flashcardRepository.findByUuid(uuid)
                .orElseThrow(() -> new NoSuchElementException("Flashcard not found"));
    }

    private Flashcard getFlashcardByUuidAndUserId(String uuid, Long userId) {
        Flashcard flashcard = getFlashcardByUuid(uuid);
        flashcard.assertOwnedBy(userId);
        return flashcard;
    }

    private FlashcardResponse mapToResponse(Flashcard flashcard) {
        FlashcardResponse response = new FlashcardResponse();
        response.setUuid(flashcard.getUuid());
        response.setTitle(flashcard.getTitle());
        response.setDescription(flashcard.getDescription());
        response.setCreatedAt(flashcard.getCreatedAt());
        response.setUpdatedAt(flashcard.getUpdatedAt());

        if (flashcard.getNotebook() != null) {
            response.setNotebookUuid(flashcard.getNotebook().getUuid());
            response.setNotebookTitle(flashcard.getNotebook().getTitle());
        }

        List<FlashcardCardResponse> cards = flashcard.getCards().stream().map(c -> {
            FlashcardCardResponse cr = new FlashcardCardResponse();
            cr.setFront(c.getFront());
            cr.setBack(c.getBack());
            return cr;
        }).toList();

        response.setCards(cards);
        response.setCardCount(cards.size());

        long attemptCount = flashcardAttemptRepository.countByFlashcardId(flashcard.getId());
        response.setAttempts(attemptCount);
        response.setBestMastery(flashcardAttemptRepository.findBestMasteryByFlashcardId(flashcard.getId()).orElse(null));

        return response;
    }
}
