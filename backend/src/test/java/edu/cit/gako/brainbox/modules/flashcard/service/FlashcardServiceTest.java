package edu.cit.gako.brainbox.modules.flashcard.service;

import edu.cit.gako.brainbox.modules.flashcard.dto.request.FlashcardAttemptRequest;
import edu.cit.gako.brainbox.modules.flashcard.dto.request.FlashcardCardRequest;
import edu.cit.gako.brainbox.modules.flashcard.dto.request.FlashcardRequest;
import edu.cit.gako.brainbox.modules.flashcard.entity.Flashcard;
import edu.cit.gako.brainbox.modules.flashcard.repository.FlashcardAttemptRepository;
import edu.cit.gako.brainbox.modules.flashcard.repository.FlashcardRepository;
import edu.cit.gako.brainbox.modules.notebook.service.NotebookService;
import edu.cit.gako.brainbox.modules.notebook.entity.Notebook;
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
class FlashcardServiceTest {

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private FlashcardAttemptRepository flashcardAttemptRepository;

    @Mock
    private NotebookService notebookService;

    @Mock
    private UserService userService;

    @InjectMocks
    private FlashcardService flashcardService;

    @Test
    void createFlashcardPersistsCardsInsideAggregate() {
        User user = new User();
        user.setId(11L);

        User owner = new User();
        owner.setId(11L);

        Notebook notebook = new Notebook();
        notebook.setUuid("nb-1");
        notebook.setUser(owner);

        FlashcardCardRequest card = new FlashcardCardRequest();
        card.setFront("Front");
        card.setBack("Back");

        FlashcardRequest request = new FlashcardRequest();
        request.setTitle("Deck");
        request.setNotebookUuid("nb-1");
        request.setCards(List.of(card));

        when(userService.findById(11L)).thenReturn(user);
        when(notebookService.getNotebookByUuid("nb-1")).thenReturn(notebook);
        when(flashcardRepository.save(any(Flashcard.class))).thenAnswer((invocation) -> {
            Flashcard flashcard = invocation.getArgument(0);
            flashcard.setId(42L);
            return flashcard;
        });
        when(flashcardAttemptRepository.countByFlashcardId(42L)).thenReturn(0L);
        when(flashcardAttemptRepository.findBestMasteryByFlashcardId(42L)).thenReturn(Optional.empty());

        flashcardService.createFlashcard(request, 11L);

        ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);
        verify(flashcardRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getCards().size());
        assertEquals("Front", captor.getValue().getCards().get(0).getFront());
        assertEquals(notebook, captor.getValue().getNotebook());
        assertEquals(user, captor.getValue().getUser());
    }

    @Test
    void recordAttemptPersistsAttemptThroughFlashcardService() {
        User user = new User();
        user.setId(11L);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(5L);
        flashcard.setUuid("flash-1");
        flashcard.setUser(user);
        flashcard.setCards(List.of());

        FlashcardAttemptRequest request = new FlashcardAttemptRequest();
        request.setMastery(4);
        request.setClientMutationId("attempt-1");

        when(flashcardRepository.findByUuid("flash-1")).thenReturn(Optional.of(flashcard));
        when(userService.findById(11L)).thenReturn(user);
        when(flashcardAttemptRepository.findByUserIdAndClientMutationId(11L, "attempt-1")).thenReturn(Optional.empty());
        when(flashcardAttemptRepository.countByFlashcardId(5L)).thenReturn(1L);
        when(flashcardAttemptRepository.findBestMasteryByFlashcardId(5L)).thenReturn(Optional.of(4));

        flashcardService.recordAttempt("flash-1", 11L, request);

        verify(flashcardAttemptRepository).save(any());
    }
}
