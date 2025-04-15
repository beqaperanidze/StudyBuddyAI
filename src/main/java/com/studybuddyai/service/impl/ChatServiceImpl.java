package com.studybuddyai.service.impl;

import com.studybuddyai.dto.ChatMessageRequest;
import com.studybuddyai.exception.DocumentNotFoundException;
import com.studybuddyai.model.ChatMessage;
import com.studybuddyai.model.Document;
import com.studybuddyai.model.User;
import com.studybuddyai.repository.ChatMessageRepository;
import com.studybuddyai.repository.DocumentRepository;
import com.studybuddyai.service.ChatService;
import com.studybuddyai.service.ClientService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

    private final ClientService clientService;
    private final ChatMessageRepository chatMessageRepository;
    private final DocumentRepository documentRepository;
    private final RestTemplate huggingFaceRestTemplate;

    @Value("${huggingface.model}")
    private String huggingfaceModel;

    @Value("${huggingface.api.url}")
    private String huggingfaceApiUrl;

    public ChatServiceImpl(ClientService clientService, ChatMessageRepository chatMessageRepository, DocumentRepository documentRepository,
                           @Qualifier("huggingFaceRestTemplate") RestTemplate huggingFaceRestTemplate) {
        this.clientService = clientService;
        this.chatMessageRepository = chatMessageRepository;
        this.documentRepository = documentRepository;
        this.huggingFaceRestTemplate = huggingFaceRestTemplate;
    }

    @Override
    @Transactional
    public ChatMessage sendMessage(ChatMessageRequest request) throws AccessDeniedException {
        User user = clientService.getAuthenticatedUser();
        Document document = documentRepository.findById(request.getDocumentId()).orElseThrow(() ->
                new DocumentNotFoundException("Document not found with Id: " + request.getDocumentId()));

        if (!document.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this document");
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setDocument(document);
        chatMessage.setUser(user);
        chatMessage.setUserQuestion(request.getUserQuestion());

        String aiResponse = generateHuggingFaceResponse(request.getUserQuestion(), document);
        chatMessage.setAiResponse(aiResponse);

        return chatMessageRepository.save(chatMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getChatHistory() {
        User user = clientService.getAuthenticatedUser();
        return chatMessageRepository.findByUserIdOrderByTimestampAsc(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessage> getChatHistoryForDocument(Long documentId) throws AccessDeniedException {
        User user = clientService.getAuthenticatedUser();
        Document document = documentRepository.findById(documentId).orElseThrow(() ->
                new DocumentNotFoundException("Document not found with Id: " + documentId));

        if (!document.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this document");
        }

        return chatMessageRepository.findByDocumentIdOrderByTimestampAsc(documentId);
    }

    @Override
    @Transactional
    public void clearChatHistory() {
        User user = clientService.getAuthenticatedUser();
        chatMessageRepository.deleteByUserId(user.getId());
    }

    @Override
    @Transactional
    public void clearChatHistoryForDocument(Long documentId) throws AccessDeniedException {
        User user = clientService.getAuthenticatedUser();
        Document document = documentRepository.findById(documentId).orElseThrow(() ->
                new DocumentNotFoundException("Document not found with Id: " + documentId));

        if (!document.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this document");
        }

        chatMessageRepository.deleteByDocumentId(documentId);
    }

    @Value("${huggingface.api.wait.time:2000}")
    private long waitTime;

    private String generateHuggingFaceResponse(String userQuestion, Document document) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", "Answer this question based on the given document: " + document.getContent() + "\n\nQuestion: " + userQuestion);

            requestBody.put("wait_for_model", false);

            String url = huggingfaceApiUrl + huggingfaceModel;

            ResponseEntity<Map> initialResponse = huggingFaceRestTemplate.postForEntity(
                    url,
                    requestBody,
                    Map.class
            );

            if (initialResponse.getStatusCode().is2xxSuccessful() &&
                    !initialResponse.getBody().containsKey("estimated_time")) {
                return String.valueOf(initialResponse.getBody().get("generated_text"));
            }

            int attempts = 10;
            while (attempts > 0) {
                Thread.sleep(waitTime);

                ResponseEntity<Map> pollResponse = huggingFaceRestTemplate.postForEntity(
                        url,
                        requestBody,
                        Map.class
                );

                if (pollResponse.getStatusCode().is2xxSuccessful() &&
                        !pollResponse.getBody().containsKey("estimated_time")) {
                    return String.valueOf(pollResponse.getBody().get("generated_text"));
                }

                attempts--;
            }

            return "Response generation took too long. Please try again later.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing request: " + e.getMessage();
        }
    }
}