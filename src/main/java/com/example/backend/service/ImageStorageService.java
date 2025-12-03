package com.example.backend.service;

import com.example.backend.model.ImageModel;
import com.example.backend.repository.ImageRepository;
import org.springframework.stereotype.Service;

@Service
public class ImageStorageService {

    private final ImageRepository repository;

    public ImageStorageService(ImageRepository repository) {
        this.repository = repository;
    }

    /**
     * Persiste a imagem após a validação do serviço Python.
     */
    public ImageModel storeImage(String originalFilename, long size, byte[] data) {
        ImageModel image = new ImageModel();
        image.setFilename(originalFilename);
        image.setSize(size);
        image.setHash("");
        image.setData(data);
        return repository.save(image);
    }
}