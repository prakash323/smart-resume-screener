package com.unthinkable.resumescreener.controller;

import com.unthinkable.resumescreener.dto.ResumeResponse;
import com.unthinkable.resumescreener.service.ResumeService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ResumeResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("candidateName") @NotBlank String candidateName,
            @RequestParam("email") @NotBlank String email,
            @RequestParam(value = "model", required = false) String model) {
        ResumeResponse response = resumeService.uploadAndProcess(file, candidateName, email, model);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ResumeResponse> listAll() {
        return resumeService.listAll();
    }

    @GetMapping("/{id}")
    public ResumeResponse getById(@PathVariable Long id) {
        return resumeService.getById(id);
    }
}
