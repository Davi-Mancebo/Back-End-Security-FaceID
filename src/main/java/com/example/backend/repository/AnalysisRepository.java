package com.example.backend.repository;

import com.example.backend.model.AnalysisModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<AnalysisModel, Long> {
}
