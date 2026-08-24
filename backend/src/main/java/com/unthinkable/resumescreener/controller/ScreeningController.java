package com.unthinkable.resumescreener.controller;

import com.unthinkable.resumescreener.dto.ScreeningRequest;
import com.unthinkable.resumescreener.dto.ScreeningResponse;
import com.unthinkable.resumescreener.dto.ShortlistEntry;
import com.unthinkable.resumescreener.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/screenings")
public class ScreeningController {

    private final ScreeningService screeningService;

    public ScreeningController(ScreeningService screeningService) {
        this.screeningService = screeningService;
    }

    @PostMapping
    public ResponseEntity<ScreeningResponse> runScreening(@Valid @RequestBody ScreeningRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(screeningService.runScreening(request));
    }

    @GetMapping("/shortlist")
    public List<ShortlistEntry> shortlist(
            @RequestParam Long jobDescriptionId,
            @RequestParam(required = false) Integer minScore) {
        return screeningService.shortlist(jobDescriptionId, minScore);
    }
}
