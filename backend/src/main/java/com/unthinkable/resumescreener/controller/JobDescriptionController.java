package com.unthinkable.resumescreener.controller;

import com.unthinkable.resumescreener.dto.JobDescriptionRequest;
import com.unthinkable.resumescreener.dto.JobDescriptionResponse;
import com.unthinkable.resumescreener.service.JobDescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-descriptions")
public class JobDescriptionController {

    private final JobDescriptionService jobDescriptionService;

    public JobDescriptionController(JobDescriptionService jobDescriptionService) {
        this.jobDescriptionService = jobDescriptionService;
    }

    @PostMapping
    public ResponseEntity<JobDescriptionResponse> create(@Valid @RequestBody JobDescriptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobDescriptionService.create(request));
    }

    @GetMapping
    public List<JobDescriptionResponse> listAll() {
        return jobDescriptionService.listAll();
    }

    @GetMapping("/{id}")
    public JobDescriptionResponse getById(@PathVariable Long id) {
        return jobDescriptionService.getById(id);
    }
}
