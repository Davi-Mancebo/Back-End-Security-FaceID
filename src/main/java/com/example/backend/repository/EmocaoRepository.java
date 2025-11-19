package com.example.backend.repository;

import com.example.backend.model.EmocaoModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmocaoRepository extends JpaRepository<EmocaoModel, Long> {
}
