package com.example.backend.repository;

import com.example.backend.model.EmotionModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmotionRepository extends JpaRepository<EmotionModel, Long> {
}
