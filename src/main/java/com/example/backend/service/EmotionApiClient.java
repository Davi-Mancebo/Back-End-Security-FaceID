package com.example.backend.service;

import com.example.backend.exception.ServiceUnavailableException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class EmotionApiClient {

    private static final Logger LOGGER = Logger.getLogger(EmotionApiClient.class.getName());
    private final RestTemplate restTemplate;
    private final String endpointUrl = "http://localhost:8000/emotion";

    public EmotionApiClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Envia a imagem para o serviço Python e retorna o resultado estruturado.
     */
    public EmotionApiResponse analyze(byte[] imageBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        byte[] payload = Objects.requireNonNull(imageBytes, "imageBytes não pode ser nulo");
        Resource fileResource = new ByteArrayResource(payload) {
            @Override
            public String getFilename() {
                return "image.jpg";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", fileResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    endpointUrl,
                    HttpMethod.POST,
                    requestEntity,
                    new ParameterizedTypeReference<>() {}
            );
            Map<String, Object> resultBody = response.getBody();
            if (resultBody == null) {
                throw new ServiceUnavailableException("API Python retornou resposta vazia.");
            }
            boolean isTarget = Boolean.TRUE.equals(resultBody.get("result"));
            Object emotionValue = resultBody.get("emotion");
            String emotion = emotionValue != null ? emotionValue.toString() : "";
            if (emotion.isBlank()) {
                throw new RuntimeException("API Python retornou emoção inválida");
            }
            Double targetScore = parseDouble(resultBody.get("target_score"));
            Map<String, Double> scores = parseScores(resultBody.get("scores"));
            return new EmotionApiResponse(isTarget, emotion, targetScore, scores);
        } catch (ServiceUnavailableException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao comunicar com API Python", e);
            throw new ServiceUnavailableException("API de análise de emoções está indisponível. Verifique se o servidor Python está rodando.");
        }
    }

    private Double parseDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Map<String, Double> parseScores(Object rawScores) {
        if (!(rawScores instanceof Map<?, ?> rawMap)) {
            return Collections.emptyMap();
        }
        Map<String, Double> normalized = new HashMap<>();
        rawMap.forEach((key, value) -> {
            if (key != null) {
                Double parsed = parseDouble(value);
                if (parsed != null) {
                    normalized.put(key.toString(), parsed);
                }
            }
        });
        return normalized;
    }

    public record EmotionApiResponse(boolean target, String emotion, Double targetScore, Map<String, Double> scores) {}
}