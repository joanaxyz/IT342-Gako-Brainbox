package edu.cit.gako.brainbox.notebook.service;

import edu.cit.gako.brainbox.auth.service.UserService;
import edu.cit.gako.brainbox.notebook.dto.request.QueueAddNotebookRequest;
import edu.cit.gako.brainbox.notebook.dto.request.QueueReorderRequest;
import edu.cit.gako.brainbox.notebook.dto.response.NotebookOverviewResponse;
import edu.cit.gako.brainbox.notebook.dto.response.PlaybackQueueResponse;
import edu.cit.gako.brainbox.notebook.entity.Notebook;
import edu.cit.gako.brainbox.notebook.entity.PlaybackQueue;
import edu.cit.gako.brainbox.notebook.repository.PlaybackQueueRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaybackQueueService {

    private final PlaybackQueueRepository queueRepository;
    private final NotebookService notebookService;
    private final UserService userService;

    public PlaybackQueueResponse getQueue(Long userId) {
        return queueRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .orElseGet(() -> emptyResponse());
    }

    @Transactional
    public PlaybackQueueResponse addNotebook(Long userId, QueueAddNotebookRequest request) {
        PlaybackQueue queue = getOrCreate(userId);
        Notebook notebook = notebookService.getNotebookByUuid(request.getNotebookUuid());
        notebook.assertOwnedBy(userId);
        if (!queue.getItems().contains(notebook)) {
            queue.getItems().add(notebook);
        }
        return mapToResponse(queueRepository.save(queue));
    }

    @Transactional
    public PlaybackQueueResponse removeNotebook(Long userId, String notebookUuid) {
        PlaybackQueue queue = getOrCreate(userId);
        Notebook notebook = notebookService.getNotebookByUuid(notebookUuid);
        queue.getItems().remove(notebook);
        return mapToResponse(queueRepository.save(queue));
    }

    @Transactional
    public void clearQueue(Long userId) {
        queueRepository.findByUserId(userId).ifPresent(queue -> {
            queue.getItems().clear();
            queueRepository.save(queue);
        });
    }

    @Transactional
    public PlaybackQueueResponse reorderQueue(Long userId, QueueReorderRequest request) {
        PlaybackQueue queue = getOrCreate(userId);
        List<Notebook> reordered = request.getNotebookUuids().stream()
                .map(notebookService::getNotebookByUuid)
                .toList();
        queue.getItems().clear();
        queue.getItems().addAll(reordered);
        return mapToResponse(queueRepository.save(queue));
    }

    private PlaybackQueue getOrCreate(Long userId) {
        return queueRepository.findByUserId(userId).orElseGet(() -> {
            PlaybackQueue queue = new PlaybackQueue();
            queue.setUser(userService.findById(userId));
            return queueRepository.save(queue);
        });
    }

    private PlaybackQueueResponse mapToResponse(PlaybackQueue queue) {
        PlaybackQueueResponse response = new PlaybackQueueResponse();
        response.setItems(queue.getItems().stream().map(this::mapNotebook).toList());
        return response;
    }

    private PlaybackQueueResponse emptyResponse() {
        PlaybackQueueResponse response = new PlaybackQueueResponse();
        response.setItems(List.of());
        return response;
    }

    private NotebookOverviewResponse mapNotebook(Notebook notebook) {
        NotebookOverviewResponse r = new NotebookOverviewResponse();
        r.setUuid(notebook.getUuid());
        r.setTitle(notebook.getTitle());
        r.setWordCount(countWords(notebook.getContent()));
        r.setCreatedAt(notebook.getCreatedAt());
        r.setUpdatedAt(notebook.getUpdatedAt());
        r.setLastReviewedAt(notebook.getLastReviewedAt());
        r.setVersion(notebook.getVersion() != null ? notebook.getVersion() : 0L);
        if (notebook.getCategory() != null) {
            r.setCategoryId(notebook.getCategory().getId());
            r.setCategoryName(notebook.getCategory().getName());
        }
        return r;
    }

    private int countWords(String html) {
        if (html == null || html.isBlank()) return 0;
        String plainText = html
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("&[^;]+;", " ")
                .trim();
        if (plainText.isBlank()) return 0;
        return plainText.split("\\s+").length;
    }
}
