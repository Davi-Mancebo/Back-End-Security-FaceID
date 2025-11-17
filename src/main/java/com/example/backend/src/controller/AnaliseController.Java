package com.example.backend.controller;

import com.example.backend.model.AnalisesModel;
import com.example.backend.service.AnalisesService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/analises")
@CrossOrigin(origins = "*")
public class AnalisesController {

    private final AnalisesService service;

    public AnalisesController(AnalisesService service) {
        this.service = service;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalisesModel upload(
            @RequestParam("dispositivo") String dispositivo,
            @RequestParam("status") boolean status,
            @RequestParam("foto") MultipartFile foto
    ) throws Exception {

        return service.salvarAnalise(dispositivo, status, foto);
    }

    @GetMapping("/{id}/foto")
    public @ResponseBody byte[] buscarImagem(@PathVariable Long id) {
        return service.buscarImagem(id);
    }
}
