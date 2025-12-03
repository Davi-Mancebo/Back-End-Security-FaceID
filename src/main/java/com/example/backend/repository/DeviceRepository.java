package com.example.backend.repository;

import com.example.backend.model.DeviceModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<DeviceModel, Long> {
    Optional<DeviceModel> findByName(String name);
}
