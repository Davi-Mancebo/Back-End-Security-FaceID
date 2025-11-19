package com.example.backend.repository;

import com.example.backend.model.ImagemModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImagemRepository extends JpaRepository<ImagemModel, Long> {
}
