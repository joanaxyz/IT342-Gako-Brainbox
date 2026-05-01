package edu.cit.gako.brainbox.modules.ai.conversation.service;

import edu.cit.gako.brainbox.modules.ai.conversation.dto.request.AiConversationRequest;
import edu.cit.gako.brainbox.modules.ai.conversation.dto.response.AiConversationResponse;
import edu.cit.gako.brainbox.modules.ai.conversation.entity.AiConversation;
import edu.cit.gako.brainbox.modules.ai.conversation.repository.AiConversationRepository;
import edu.cit.gako.brainbox.modules.user.service.UserService;
import edu.cit.gako.brainbox.modules.user.entity.User;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiConversationService {

    private final AiConversationRepository repository;
    private final UserService userService;

    public List<AiConversationResponse> getConversations(Long userId, String notebookUuid) {
        return repository.findByUserIdAndNotebookUuidOrderByUpdatedAtDesc(userId, notebookUuid)
                .stream()
                .map(AiConversationResponse::new)
                .toList();
    }

    @Transactional
    public AiConversationResponse save(Long userId, AiConversationRequest request) {
        User user = userService.findById(userId);
        AiConversation conv = new AiConversation();
        conv.setUser(user);
        conv.setNotebookUuid(request.getNotebookUuid());
        conv.setMode(request.getMode());
        conv.setTitle(request.getTitle());
        conv.setMessages(request.getMessages());
        return new AiConversationResponse(repository.save(conv));
    }

    @Transactional
    public AiConversationResponse update(Long userId, String uuid, AiConversationRequest request) {
        AiConversation conv = repository.findByUuidAndUserId(uuid, userId)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found"));
        conv.setTitle(request.getTitle());
        conv.setMessages(request.getMessages());
        return new AiConversationResponse(repository.save(conv));
    }

    @Transactional
    public void delete(Long userId, String uuid) {
        if (repository.findByUuidAndUserId(uuid, userId).isEmpty()) {
            throw new NoSuchElementException("Conversation not found");
        }
        repository.deleteByUuidAndUserId(uuid, userId);
    }

    public List<AiConversationResponse> getAllConversations() {
        return repository.findAll().stream()
                .map(AiConversationResponse::new)
                .toList();
    }

    public AiConversationResponse getConversation(String uuid) {
        return repository.findByUuid(uuid)
                .map(AiConversationResponse::new)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found"));
    }

    @Transactional
    public void deleteAsAdmin(String uuid) {
        AiConversation conversation = repository.findByUuid(uuid)
                .orElseThrow(() -> new NoSuchElementException("Conversation not found"));
        repository.delete(conversation);
    }
}
