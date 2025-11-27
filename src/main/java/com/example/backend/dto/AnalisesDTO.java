package com.example.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AnalisesDTO {
    private Long id;
    private String dispositivo;
    private boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imagemBase64;
}