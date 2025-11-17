package com.example.backend.service;

import com.example.backend.model.AnalisesModel;
import com.example.backend.repository.AnalisesRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
}
