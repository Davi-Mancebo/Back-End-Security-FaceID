package com.example.backend.repository;

import com.example.backend.model.ResultModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepository extends JpaRepository<ResultModel, Long> {
}
