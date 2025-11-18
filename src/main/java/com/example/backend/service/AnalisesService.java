package com.example.backend.service;

import com.example.backend.dto.AnalisesDTO;
import com.example.backend.model.AnalisesModel;
import com.example.backend.repository.AnalisesRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalisesService {

    private final AnalisesRepository repository;

    public AnalisesService(AnalisesRepository repository) {
        this.repository = repository;
    }

    public AnalisesModel salvarAnalise(String dispositivo, boolean status, MultipartFile foto) throws Exception {
        AnalisesModel model = new AnalisesModel();
        model.setDispositivo(dispositivo);
        model.setStatus(status);
        model.setImagem(foto.getBytes()); // <-- transforma MultipartFile em byte[]
        return repository.save(model);
    }

    public byte[] buscarImagem(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"))
                .getImagem();
    }

    public List<AnalisesDTO> listarTudoDTO() {
        return repository.findAll().stream().map(model -> {
            AnalisesDTO dto = new AnalisesDTO();
            dto.setId(model.getId());
            dto.setDispositivo(model.getDispositivo());
            dto.setStatus(model.isStatus());
            dto.setCreatedAt(model.getCreatedAt());
            dto.setUpdatedAt(model.getUpdatedAt());

            // Converter para Base64
            dto.setImagemBase64(Base64.getEncoder().encodeToString(model.getImagem()));

            return dto;
        }).collect(Collectors.toList());
    }
}
