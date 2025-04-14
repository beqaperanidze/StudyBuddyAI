package com.studybuddyai.service;

import com.studybuddyai.model.ChatMessage;
import com.studybuddyai.dto.ChatMessageRequest;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ChatService {

    ChatMessage sendMessage(ChatMessageRequest request) throws AccessDeniedException;

    List<ChatMessage> getChatHistoryForDocument(Long documentId) throws AccessDeniedException;

    List<ChatMessage> getChatHistory();

    void clearChatHistory();

    void clearChatHistoryForDocument(Long documentId) throws AccessDeniedException;
}
