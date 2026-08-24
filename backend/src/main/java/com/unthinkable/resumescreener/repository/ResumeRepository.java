package com.unthinkable.resumescreener.repository;

import com.unthinkable.resumescreener.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
