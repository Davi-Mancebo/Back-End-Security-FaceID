package com.example.backend.repository;

import com.example.backend.model.ResultadoModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultadoRepository extends JpaRepository<ResultadoModel, Long> {
}
