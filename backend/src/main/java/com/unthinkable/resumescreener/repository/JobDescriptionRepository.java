package com.unthinkable.resumescreener.repository;

import com.unthinkable.resumescreener.domain.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {
}
