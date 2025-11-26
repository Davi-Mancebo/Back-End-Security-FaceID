package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.Base64;

import com.example.backend.model.AnalisesModel;
import lombok.Data;

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

        if (model.getDispositivo() != null) {
            dto.setDispositivo(model.getDispositivo().getNome());
        }

        dto.setStatus(model.isStatus());
        dto.setCreatedAt(model.getCreatedAt());
        dto.setUpdatedAt(model.getUpdatedAt());

        if (model.getImagem() != null && model.getImagem().getDados() != null) {
            dto.setImagemBase64(
                    Base64.getEncoder().encodeToString(model.getImagem().getDados())
            );
        }

        return dto;
    }
}