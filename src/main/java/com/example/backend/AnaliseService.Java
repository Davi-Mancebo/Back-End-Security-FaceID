package com.example.backend.service;

import com.example.backend.src.model.Analise;
import com.example.backend.src.repository.AnaliseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnaliseService {

    private final AnaliseRepository repository;

    public AnaliseService(AnaliseRepository repository) {
        this.repository = repository;
    }

    public Analise criar(Analise analise) {
        return repository.save(analise);
    }

    public List<Analise> listar() {
        return repository.findAll();
    }

    public Analise buscarPorId(Long id) {
        return repository.findById(id).orElse(null);
    }
}