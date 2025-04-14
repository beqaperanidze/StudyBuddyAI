package com.studybuddyai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private Long id;
    private String userQuestion;
    private String aiResponse;
    private Long documentId;
    private String documentName;
    private LocalDateTime timestamp;
}