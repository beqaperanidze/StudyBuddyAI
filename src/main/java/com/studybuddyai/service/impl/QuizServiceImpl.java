package com.studybuddyai.service.impl;

import com.studybuddyai.dto.QuizDto;
import com.studybuddyai.dto.QuizGenerationRequest;
import com.studybuddyai.exception.DocumentNotFoundException;
import com.studybuddyai.model.Document;
import com.studybuddyai.model.Quiz;
import com.studybuddyai.model.QuizQuestion;
import com.studybuddyai.model.User;
import com.studybuddyai.repository.DocumentRepository;
import com.studybuddyai.repository.QuizRepository;
import com.studybuddyai.service.ClientService;
import com.studybuddyai.service.QuizService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuizServiceImpl implements QuizService {
    private final ClientService clientService;
    private final QuizRepository quizRepository;
    private final DocumentRepository documentRepository;

    public QuizServiceImpl(ClientService clientService, QuizRepository quizRepository, DocumentRepository documentRepository) {
        this.clientService = clientService;
        this.quizRepository = quizRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional
    public QuizDto generateQuizFromDocument(Long documentId, QuizGenerationRequest request) throws AccessDeniedException {
        User user = clientService.getAuthenticatedUser();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        if (!document.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to access this document");
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setDocument(document);

        List<QuizQuestion> questions = new ArrayList<>();
        for (int i = 1; i <= request.getNumberOfQuestions(); i++) {
            QuizQuestion question = new QuizQuestion();
            question.setQuiz(quiz);
            question.setQuestion("Sample Question " + i);
            question.setOptions(List.of("Option A", "Option B", "Option C", "Option D"));
            question.setCorrectAnswer("Option A");
            questions.add(question);
        }

        quiz.setQuestions(questions);

        Quiz savedQuiz = quizRepository.save(quiz);
        return convertToDto(savedQuiz);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizDto> getAllQuizzesByUser() {
        User user = clientService.getAuthenticatedUser();
        List<Document> userDocuments = user.getDocuments();
        List<Quiz> quizzes = new ArrayList<>();
        for (Document doc : userDocuments) {
            quizzes.addAll(doc.getQuizzes());
        }

        return quizzes.stream().map(this::convertToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDto getQuizById(Long quizId) {
        User user = clientService.getAuthenticatedUser();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        if (!quiz.getDocument().getUser().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Unauthorized access to this quiz");
        }
        return convertToDto(quiz);
    }

    @Override
    @Transactional
    public void deleteQuiz(Long quizId) {

        User user = clientService.getAuthenticatedUser();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (!quiz.getDocument().getUser().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Unauthorized access to this quiz");
        }

        quizRepository.delete(quiz);
    }

    private QuizDto convertToDto(Quiz quiz) {
        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        return dto;
    }
}
