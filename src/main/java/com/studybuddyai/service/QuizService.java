package com.studybuddyai.service;

import com.studybuddyai.dto.QuizDto;
import com.studybuddyai.dto.QuizGenerationRequest;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public interface QuizService {

    QuizDto generateQuizFromDocument(Long documentId, QuizGenerationRequest request) throws AccessDeniedException;

    List<QuizDto> getAllQuizzesByUser();

    QuizDto getQuizById(Long quizId);

    void deleteQuiz(Long quizId);
}
