package com.studybuddyai.dto;

import lombok.Data;

@Data
public class QuizGenerationRequest {
    private int numberOfQuestions;
    private String title;
    private String difficulty;
    private String topic;
}
