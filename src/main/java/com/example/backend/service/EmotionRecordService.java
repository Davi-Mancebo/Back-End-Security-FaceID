package com.example.backend.service;

import com.example.backend.model.EmotionModel;
import com.example.backend.repository.EmotionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmotionRecordService {

    private final EmotionRepository repository;

    public EmotionRecordService(EmotionRepository repository) {
        this.repository = repository;
    }

    /**
     * Cria e persiste o registro da emoção dominante retornada pelo Python.
     */
    public EmotionModel saveEmotion(String emotionName) {
        EmotionModel emotion = new EmotionModel();
        emotion.setName(emotionName);
        emotion.setScore(null);
        emotion.setOccurredAt(LocalDateTime.now());
        return repository.save(emotion);
    }
}