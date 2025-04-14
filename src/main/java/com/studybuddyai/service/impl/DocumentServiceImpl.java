package com.studybuddyai.service.impl;

import com.studybuddyai.dto.DocumentDto;
import com.studybuddyai.exception.DocumentNotFoundException;
import com.studybuddyai.model.Document;
import com.studybuddyai.model.User;
import com.studybuddyai.repository.DocumentRepository;
import com.studybuddyai.service.ClientService;
import com.studybuddyai.service.DocumentService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final DocumentRepository documentRepository;
    private final ClientService clientService;

    public DocumentServiceImpl(DocumentRepository documentRepository, ClientService clientService) {

        this.documentRepository = documentRepository;
        this.clientService = clientService;
    }

    @Override
    @Transactional
    public DocumentDto uploadAndParseDocument(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty");
        }

        User user = clientService.getAuthenticatedUser();

        try (var inputStream = file.getInputStream()) {
            var pdfDocument = PDDocument.load(inputStream);
            var pdfStripper = new PDFTextStripper();
            String parsedText = pdfStripper.getText(pdfDocument);
            pdfDocument.close();

            Document document = new Document();
            document.setName(file.getOriginalFilename());
            document.setOriginalFileName(file.getOriginalFilename());
            document.setContent(parsedText);
            document.setUser(user);

            Document savedDocument = documentRepository.save(document);
            return convertToDto(savedDocument);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read and parse PDF", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDto> getMyDocuments() {
        User user = clientService.getAuthenticatedUser();
        return user.getDocuments().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDto getMyDocumentById(Long id) {
        User user = clientService.getAuthenticatedUser();

        Document document = user.getDocuments().stream()
                .filter(doc -> Objects.equals(doc.getId(), id))
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException("Document with ID " + id + " not found for current user."));

        return convertToDto(document);
    }

    @Override
    @Transactional
    public void deleteMyDocument(Long id) {
        User user = clientService.getAuthenticatedUser();
        Document deletedDocument = user.getDocuments().stream()
                .filter(document -> Objects.equals(document.getId(), id))
                .findFirst()
                .orElseThrow(() -> new DocumentNotFoundException("Document with ID " + id + " not found for current user."));
        documentRepository.delete(deletedDocument);
    }


    private DocumentDto convertToDto(Document document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setName(document.getName());
        dto.setOriginalFileName(document.getOriginalFileName());
        dto.setUploadedAt(document.getUploadedAt());
        return dto;
    }

}
