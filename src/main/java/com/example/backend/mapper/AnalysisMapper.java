package com.example.backend.mapper;

import com.example.backend.dto.AnalysisDTO;
import com.example.backend.model.AnalysisModel;
import com.example.backend.model.ResultModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class AnalysisMapper {

    private static final Logger LOGGER = Logger.getLogger(AnalysisMapper.class.getName());
    private final ObjectMapper objectMapper;

    public AnalysisMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Converte a entidade em DTO pronto para o frontend (imagem em Base64).
     */
    public AnalysisDTO toDto(AnalysisModel model) {
        AnalysisDTO dto = new AnalysisDTO();
        dto.setId(model.getId());
        dto.setDevice(model.getDevice() != null ? model.getDevice().getName() : null);
        dto.setStatus(model.isStatus());
        dto.setCreatedAt(model.getCreatedAt());
        dto.setUpdatedAt(model.getUpdatedAt());
        dto.setImageBase64(extractImage(model));
        dto.setDominantEmotion(model.getEmotion() != null ? model.getEmotion().getName() : null);
        dto.setTargetScore(resolveTargetScore(model));
        dto.setEmotionScores(resolveScores(model));
        return dto;
    }

    private String extractImage(AnalysisModel model) {
        if (model.getImage() == null || model.getImage().getData() == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(model.getImage().getData());
    }

    private Double resolveTargetScore(AnalysisModel model) {
        ResultModel result = model.getResult();
        return result != null ? result.getTargetScore() : null;
    }

    private Map<String, Double> resolveScores(AnalysisModel model) {
        ResultModel result = model.getResult();
        if (result == null || result.getEmotionScoresJson() == null) {
            return null;
        }
        try {
            return objectMapper.readValue(result.getEmotionScoresJson(), new TypeReference<>() {});
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Falha ao decodificar emotionScores", e);
            return null;
        }
    }
}