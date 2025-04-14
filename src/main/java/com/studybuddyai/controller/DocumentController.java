package com.studybuddyai.controller;

import com.studybuddyai.model.Document;
import com.studybuddyai.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file) {
        Document uploaded = documentService.uploadAndParseDocument(file);
        return ResponseEntity.ok(uploaded);
    }

    @GetMapping("/me")
    public ResponseEntity<List<Document>> getMyDocuments() {
        return ResponseEntity.ok(documentService.getMyDocuments());
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<Document> getMyDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getMyDocumentById(id));
    }

    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> deleteMyDocument(@PathVariable Long id) {
        documentService.deleteMyDocument(id);
        return ResponseEntity.noContent().build();
    }
}
