package edu.cit.gako.brainbox.notebook.dto.response;

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
