package com.example.backend.repository;

import com.example.backend.model.ProcessingLogModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessingLogRepository extends JpaRepository<ProcessingLogModel, Long> {
}
