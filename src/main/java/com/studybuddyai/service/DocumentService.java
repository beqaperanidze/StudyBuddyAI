package com.studybuddyai.service;

import com.studybuddyai.dto.DocumentDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface DocumentService {
    DocumentDto uploadAndParseDocument(MultipartFile file);
    List<DocumentDto> getMyDocuments();
    DocumentDto getMyDocumentById(Long id);
    void deleteMyDocument(Long id);
}