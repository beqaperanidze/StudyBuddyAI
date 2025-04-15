package com.studybuddyai.service.impl;

import com.studybuddyai.dto.ChatMessageRequest;
import com.studybuddyai.dto.ChatMessageRequestWithModel;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final ClientService clientService;
    private final ChatMessageRepository chatMessageRepository;
    private final DocumentRepository documentRepository;
    private final RestTemplate restTemplate;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;

    public ChatServiceImpl(ClientService clientService, ChatMessageRepository chatMessageRepository, DocumentRepository documentRepository, @Qualifier("openaiRestTemplate") RestTemplate restTemplate) {
        this.clientService = clientService;
        this.chatMessageRepository = chatMessageRepository;
        this.documentRepository = documentRepository;
        this.restTemplate = restTemplate;
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

        String aiResponse = generateAIResponse(request.getUserQuestion(), document);
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

    private String generateAIResponse(String userQuestion, Document document) {
        try {
            ChatMessageRequestWithModel chatMessageRequest = new ChatMessageRequestWithModel
                    (model, "Answer this question based on given document: " + userQuestion, document.getId());
            return restTemplate.postForObject(apiUrl, chatMessageRequest, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return " ";
    }
}