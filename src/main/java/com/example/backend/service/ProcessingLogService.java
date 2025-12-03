package com.example.backend.service;

import com.example.backend.model.ProcessingLogModel;
import com.example.backend.repository.ProcessingLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProcessingLogService {

    private final ProcessingLogRepository repository;

    public ProcessingLogService(ProcessingLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra logs de sucesso para auditoria.
     */
    public ProcessingLogModel registerSuccess(String message) {
        return saveLog("OK", message);
    }

    /**
     * Registra logs de erro mesmo quando ocorre uma exceção durante a análise.
     */
    public ProcessingLogModel registerFailure(String message) {
        return saveLog("ERROR", message);
    }

    private ProcessingLogModel saveLog(String status, String message) {
        ProcessingLogModel log = new ProcessingLogModel();
        log.setTimestamp(LocalDateTime.now());
        log.setStatus(status);
        log.setMessage(message);
        return repository.save(log);
    }
}