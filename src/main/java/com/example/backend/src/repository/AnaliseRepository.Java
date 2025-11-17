package com.example.backend.repository;

import com.example.backend.model.AnalisesModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalisesRepository extends JpaRepository<AnalisesModel, Long> {
}
