package com.example.backend.repository;

import com.example.backend.model.DispositivoModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispositivoRepository extends JpaRepository<DispositivoModel, Long> {
    DispositivoModel findByNome(String nome);
}
