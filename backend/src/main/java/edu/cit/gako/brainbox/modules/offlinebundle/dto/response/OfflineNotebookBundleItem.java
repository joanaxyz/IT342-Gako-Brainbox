package edu.cit.gako.brainbox.modules.offlinebundle.dto.response;

import edu.cit.gako.brainbox.modules.flashcard.dto.response.FlashcardResponse;
import edu.cit.gako.brainbox.modules.notebook.dto.response.NotebookFullResponse;
import edu.cit.gako.brainbox.modules.playlist.dto.response.PlaylistResponse;
import edu.cit.gako.brainbox.modules.quiz.dto.response.QuizResponse;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfflineNotebookBundleItem {
    private NotebookFullResponse notebook;
    private List<QuizResponse> quizzes;
    private List<FlashcardResponse> flashcards;
    private List<PlaylistResponse> playlists;
}
