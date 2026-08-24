package com.unthinkable.resumescreener.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "job_descriptions")
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String rawText;

    private Instant createdAt;

    protected JobDescription() {
    }

    public JobDescription(String title, String rawText) {
        this.title = title;
        this.rawText = rawText;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getRawText() {
        return rawText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
