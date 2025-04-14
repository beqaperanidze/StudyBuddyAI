package com.studybuddyai.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentDto {
    private Long id;
    private String name;
    private String originalFileName;
    private LocalDateTime uploadedAt;
}