package com.studybuddyai.controller;

import com.studybuddyai.dto.ChatMessageRequest;
import com.studybuddyai.dto.ChatMessageResponse;
import com.studybuddyai.model.ChatMessage;
import com.studybuddyai.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request) {
        try {
            ChatMessage chatMessage = chatService.sendMessage(request);
            return ResponseEntity.ok(convertToResponse(chatMessage));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory() {
        List<ChatMessage> chatMessages = chatService.getChatHistory();
        List<ChatMessageResponse> responseList = chatMessages.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/history/document/{documentId}")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistoryForDocument(@PathVariable Long documentId) {
        try {
            List<ChatMessage> chatMessages = chatService.getChatHistoryForDocument(documentId);
            List<ChatMessageResponse> responseList = chatMessages.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseList);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearChatHistory() {
        try {
            chatService.clearChatHistory();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/history/document/{documentId}")
    public ResponseEntity<Void> clearChatHistoryForDocument(@PathVariable Long documentId) {
        try {
            chatService.clearChatHistoryForDocument(documentId);
            return ResponseEntity.noContent().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ChatMessageResponse convertToResponse(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .userQuestion(chatMessage.getUserQuestion())
                .aiResponse(chatMessage.getAiResponse())
                .documentId(chatMessage.getDocument().getId())
                .documentName(chatMessage.getDocument().getName())
                .timestamp(chatMessage.getTimestamp())
                .build();
    }
}