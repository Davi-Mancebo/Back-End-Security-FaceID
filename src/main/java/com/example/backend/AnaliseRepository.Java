package com.example.backend.repository;

import com.example.backend.src.model.Analise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnaliseRepository extends JpaRepository<Analise, Long> {
    // se precisar de queries por status, dispositivo, etc, a gente adiciona aqui
}