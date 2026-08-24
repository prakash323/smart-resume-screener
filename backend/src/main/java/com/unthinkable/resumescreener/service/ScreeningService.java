package com.unthinkable.resumescreener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unthinkable.resumescreener.config.OpenRouterProperties;
import com.unthinkable.resumescreener.domain.JobDescription;
import com.unthinkable.resumescreener.domain.MatchResult;
import com.unthinkable.resumescreener.domain.Resume;
import com.unthinkable.resumescreener.dto.*;
import com.unthinkable.resumescreener.exception.ResourceNotFoundException;
import com.unthinkable.resumescreener.repository.MatchResultRepository;
import com.unthinkable.resumescreener.repository.ResumeRepository;
import com.unthinkable.resumescreener.scoring.ScoringService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class ScreeningService {

    private final ResumeRepository resumeRepository;
    private final JobDescriptionService jobDescriptionService;
    private final MatchResultRepository matchResultRepository;
    private final ScoringService scoringService;
    private final ObjectMapper objectMapper;
    private final OpenRouterProperties properties;

    public ScreeningService(ResumeRepository resumeRepository,
                             JobDescriptionService jobDescriptionService,
                             MatchResultRepository matchResultRepository,
                             ScoringService scoringService,
                             ObjectMapper objectMapper,
                             OpenRouterProperties properties) {
        this.resumeRepository = resumeRepository;
        this.jobDescriptionService = jobDescriptionService;
        this.matchResultRepository = matchResultRepository;
        this.scoringService = scoringService;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Transactional
    public ScreeningResponse runScreening(ScreeningRequest request) {
        Resume resume = resumeRepository.findById(request.resumeId())
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + request.resumeId()));
        JobDescription jobDescription = jobDescriptionService.getEntity(request.jobDescriptionId());

        ExtractedResumeData extracted = readJson(resume.getExtractedDataJson());
        String model = (request.model() == null || request.model().isBlank()) ? properties.getDefaultModel() : request.model();

        LlmScoreResult result = scoringService.score(resume.getRawText(), extracted, jobDescription.getRawText(), model);

        MatchResult saved = matchResultRepository.save(
                new MatchResult(resume, jobDescription, result.score(), result.justification(), model)
        );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ShortlistEntry> shortlist(Long jobDescriptionId, Integer minScore) {
        return matchResultRepository.findByJobDescriptionIdOrderByScoreDesc(jobDescriptionId).stream()
                .filter(m -> minScore == null || m.getScore() >= minScore)
                .map(m -> new ShortlistEntry(
                        m.getResume().getId(),
                        m.getResume().getCandidateName(),
                        m.getResume().getEmail(),
                        m.getScore(),
                        m.getJustification(),
                        m.getModelUsed()
                ))
                .toList();
    }

    private ScreeningResponse toResponse(MatchResult m) {
        return new ScreeningResponse(
                m.getId(),
                m.getResume().getId(),
                m.getJobDescription().getId(),
                m.getScore(),
                m.getJustification(),
                m.getModelUsed(),
                m.getCreatedAt()
        );
    }

    private ExtractedResumeData readJson(String json) {
        try {
            return objectMapper.readValue(json, ExtractedResumeData.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
