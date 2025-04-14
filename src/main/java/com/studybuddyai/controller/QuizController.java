package com.studybuddyai.controller;

import com.studybuddyai.dto.QuizDto;
import com.studybuddyai.dto.QuizGenerationRequest;
import com.studybuddyai.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/generate/{documentId}")
    public ResponseEntity<QuizDto> generateQuiz(@PathVariable Long documentId,
                                                @RequestBody QuizGenerationRequest request) throws AccessDeniedException {
        QuizDto quizDto = quizService.generateQuizFromDocument(documentId, request);
        return ResponseEntity.ok(quizDto);
    }

    @GetMapping
    public ResponseEntity<List<QuizDto>> getAllQuizzes() {
        List<QuizDto> quizzes = quizService.getAllQuizzesByUser();
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDto> getQuizById(@PathVariable Long quizId) {
        QuizDto quizDto = quizService.getQuizById(quizId);
        return ResponseEntity.ok(quizDto);
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build();
    }
}
