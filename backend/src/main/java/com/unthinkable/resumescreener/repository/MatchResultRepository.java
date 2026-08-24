package com.unthinkable.resumescreener.repository;

import com.unthinkable.resumescreener.domain.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    List<MatchResult> findByJobDescriptionIdOrderByScoreDesc(Long jobDescriptionId);
}
