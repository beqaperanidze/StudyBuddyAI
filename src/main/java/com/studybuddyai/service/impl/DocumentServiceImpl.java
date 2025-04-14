package com.studybuddyai.service.impl;

import com.studybuddyai.dto.UserDto;
import com.studybuddyai.exception.DocumentNotFoundException;
import com.studybuddyai.exception.UserNotFoundException;
import com.studybuddyai.model.Document;
import com.studybuddyai.model.User;
import com.studybuddyai.repository.DocumentRepository;
import com.studybuddyai.repository.UserRepository;
import com.studybuddyai.service.ClientService;
import com.studybuddyai.service.DocumentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final ClientService clientService;

    public DocumentServiceImpl(UserRepository userRepository, DocumentRepository documentRepository, ClientService clientService) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.clientService = clientService;
    }

    @Override
    public Document uploadAndParseDocument(MultipartFile file) {
        return null;
    }

    @Override
    public List<Document> getMyDocuments() {
        User user = getAuthenticatedUser();
        return user.getDocuments();
    }

    @Override
    public Document getMyDocumentById(Long id) {
        User user = getAuthenticatedUser();

        return user.getDocuments().stream()
                .filter(document -> Objects.equals(document.getId(), id))
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException("Document with ID " + id + " not found for current user."));

    }

    @Override
    public void deleteMyDocument(Long id) {
        User user = getAuthenticatedUser();
        Document deletedDocument = user.getDocuments().stream()
                .filter(document -> Objects.equals(document.getId(), id))
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException("Document with ID " + id + " not found for current user."));
        documentRepository.delete(deletedDocument);
    }

    private User getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth.getPrincipal() instanceof OAuth2User oauthUser)) {
            throw new RuntimeException("Invalid principal type");
        }

        UserDto currentUser = clientService.getCurrentUser(oauthUser);
        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }

        return userRepository.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found!"));
    }

}
