package com.unthinkable.resumescreener.service;

import com.unthinkable.resumescreener.domain.JobDescription;
import com.unthinkable.resumescreener.dto.JobDescriptionRequest;
import com.unthinkable.resumescreener.dto.JobDescriptionResponse;
import com.unthinkable.resumescreener.exception.ResourceNotFoundException;
import com.unthinkable.resumescreener.repository.JobDescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobDescriptionService {

    private final JobDescriptionRepository jobDescriptionRepository;

    public JobDescriptionService(JobDescriptionRepository jobDescriptionRepository) {
        this.jobDescriptionRepository = jobDescriptionRepository;
    }

    public JobDescriptionResponse create(JobDescriptionRequest request) {
        JobDescription saved = jobDescriptionRepository.save(new JobDescription(request.title(), request.rawText()));
        return toResponse(saved);
    }

    public JobDescriptionResponse getById(Long id) {
        return jobDescriptionRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Job description not found: " + id));
    }

    public List<JobDescriptionResponse> listAll() {
        return jobDescriptionRepository.findAll().stream().map(this::toResponse).toList();
    }

    JobDescription getEntity(Long id) {
        return jobDescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job description not found: " + id));
    }

    private JobDescriptionResponse toResponse(JobDescription entity) {
        return new JobDescriptionResponse(entity.getId(), entity.getTitle(), entity.getRawText(), entity.getCreatedAt());
    }
}
