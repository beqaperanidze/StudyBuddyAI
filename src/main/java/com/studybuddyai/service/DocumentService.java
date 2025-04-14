package com.studybuddyai.service;

import com.studybuddyai.model.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface DocumentService {

    Document uploadAndParseDocument(MultipartFile file);

    List<Document> getMyDocuments();

    Document getMyDocumentById(Long id);

    void deleteMyDocument(Long id);
}
