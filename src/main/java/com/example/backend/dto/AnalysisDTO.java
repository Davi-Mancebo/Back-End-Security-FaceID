package com.example.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AnalysisDTO {
    private Long id;
    private String device;
    private boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imageBase64;
    private String dominantEmotion;
    private Double targetScore;
    private Map<String, Double> emotionScores;
}