package com.example.backend.service;

import com.example.backend.model.ResultModel;
import com.example.backend.repository.ResultRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ResultRecordService {

    private final ResultRepository repository;
    private final ObjectMapper objectMapper;

    public ResultRecordService(ResultRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Persiste o resultado (Target/Normal) com uma descrição amigável.
     */
    public ResultModel saveResult(EmotionApiClient.EmotionApiResponse response) {
        ResultModel result = new ResultModel();
        result.setOutcome(response.target() ? "Target" : "Normal");
        result.setDetails(buildDetails(response));
        result.setTargetScore(response.targetScore());
        result.setEmotionScoresJson(serializeScores(response.scores()));
        return repository.save(result);
    }

    private String buildDetails(EmotionApiClient.EmotionApiResponse response) {
        StringBuilder builder = new StringBuilder("Emoção dominante: ")
                .append(response.emotion() == null ? "desconhecida" : response.emotion());
        if (response.targetScore() != null) {
            builder.append(" | target_score=")
                    .append(String.format("%.2f", response.targetScore()));
        }
        return builder.toString();
    }

    private String serializeScores(Map<String, Double> scores) {
        if (scores == null || scores.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(scores);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Erro ao serializar mapa de emoções", e);
        }
    }
}