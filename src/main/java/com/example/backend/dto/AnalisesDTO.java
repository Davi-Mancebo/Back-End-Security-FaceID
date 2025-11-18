package com.example.backend.dto;

import java.time.LocalDateTime;

import com.example.backend.model.AnalisesModel;

import lombok.Data;

import java.util.Base64;

@Data
public class AnalisesDTO {

    private Long id;
    private String dispositivo;
    private boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String imagemBase64;

    public static AnalisesDTO fromModel(AnalisesModel model) {
        AnalisesDTO dto = new AnalisesDTO();
        dto.setId(model.getId());
        dto.setDispositivo(model.getDispositivo());
        dto.setStatus(model.isStatus());
        dto.setCreatedAt(model.getCreatedAt());
        dto.setUpdatedAt(model.getUpdatedAt());

        dto.setImagemBase64(Base64.getEncoder().encodeToString(model.getImagem()));

        return dto;
    }
}
