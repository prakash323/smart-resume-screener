package com.unthinkable.resumescreener.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "match_results")
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "job_description_id")
    private JobDescription jobDescription;

    private int score;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String justification;

    private String modelUsed;

    private Instant createdAt;

    protected MatchResult() {
    }

    public MatchResult(Resume resume, JobDescription jobDescription, int score, String justification, String modelUsed) {
        this.resume = resume;
        this.jobDescription = jobDescription;
        this.score = score;
        this.justification = justification;
        this.modelUsed = modelUsed;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Resume getResume() {
        return resume;
    }

    public JobDescription getJobDescription() {
        return jobDescription;
    }

    public int getScore() {
        return score;
    }

    public String getJustification() {
        return justification;
    }

    public String getModelUsed() {
        return modelUsed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
