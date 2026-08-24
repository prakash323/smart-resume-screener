package com.unthinkable.resumescreener.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String candidateName;

    private String email;

    private String fileName;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String rawText;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String extractedDataJson;

    private Instant createdAt;

    protected Resume() {
    }

    public Resume(String candidateName, String email, String fileName, String rawText, String extractedDataJson) {
        this.candidateName = candidateName;
        this.email = email;
        this.fileName = fileName;
        this.rawText = rawText;
        this.extractedDataJson = extractedDataJson;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public String getEmail() {
        return email;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRawText() {
        return rawText;
    }

    public String getExtractedDataJson() {
        return extractedDataJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
