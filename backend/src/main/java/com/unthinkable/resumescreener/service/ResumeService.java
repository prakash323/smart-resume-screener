package com.unthinkable.resumescreener.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unthinkable.resumescreener.domain.Resume;
import com.unthinkable.resumescreener.dto.ExtractedResumeData;
import com.unthinkable.resumescreener.dto.ResumeResponse;
import com.unthinkable.resumescreener.exception.ResourceNotFoundException;
import com.unthinkable.resumescreener.extraction.ExtractionService;
import com.unthinkable.resumescreener.parsing.ResumeParsingService;
import com.unthinkable.resumescreener.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class ResumeService {

    private final ResumeParsingService parsingService;
    private final ExtractionService extractionService;
    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    public ResumeService(ResumeParsingService parsingService,
                          ExtractionService extractionService,
                          ResumeRepository resumeRepository,
                          ObjectMapper objectMapper) {
        this.parsingService = parsingService;
        this.extractionService = extractionService;
        this.resumeRepository = resumeRepository;
        this.objectMapper = objectMapper;
    }

    public ResumeResponse uploadAndProcess(MultipartFile file, String candidateName, String email, String model) {
        String rawText = parsingService.extractText(file);
        ExtractedResumeData extracted = extractionService.extract(rawText, model);

        Resume resume = new Resume(candidateName, email, file.getOriginalFilename(), rawText, writeJson(extracted));
        resume = resumeRepository.save(resume);

        return toResponse(resume, extracted);
    }

    public ResumeResponse getById(Long id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + id));
        return toResponse(resume, readJson(resume.getExtractedDataJson()));
    }

    public List<ResumeResponse> listAll() {
        return resumeRepository.findAll().stream()
                .map(r -> toResponse(r, readJson(r.getExtractedDataJson())))
                .toList();
    }

    private ResumeResponse toResponse(Resume resume, ExtractedResumeData extracted) {
        return new ResumeResponse(
                resume.getId(),
                resume.getCandidateName(),
                resume.getEmail(),
                resume.getFileName(),
                extracted,
                resume.getCreatedAt()
        );
    }

    private String writeJson(ExtractedResumeData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private ExtractedResumeData readJson(String json) {
        try {
            return objectMapper.readValue(json, ExtractedResumeData.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
