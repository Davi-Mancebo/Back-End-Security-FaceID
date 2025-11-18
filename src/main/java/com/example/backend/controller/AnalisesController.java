package com.example.backend.controller;

import com.example.backend.dto.AnalisesDTO;
import com.example.backend.model.AnalisesModel;
import com.example.backend.service.AnalisesService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/analises")
@CrossOrigin(origins = "*")
public class AnalisesController {

    private final AnalisesService service;

    public AnalisesController(AnalisesService service) {
        this.service = service;
    }

    // LISTA TUDO COMO DTO (imagem em Base64)
    @GetMapping
    public List<AnalisesDTO> listarTudo() {
        return service.listarTudoDTO();
    }

    // UPLOAD
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalisesDTO upload(
            @RequestParam("dispositivo") String dispositivo,
            @RequestParam("status") boolean status,
            @RequestParam("imagem") MultipartFile foto
    ) throws Exception {

        AnalisesModel model = service.salvarAnalise(dispositivo, status, foto);
        return AnalisesDTO.fromModel(model);
    }

    // BUSCA A FOTO BRUTA
    @GetMapping("/{id}/foto")
    public @ResponseBody byte[] buscarImagem(@PathVariable Long id) {
        return service.buscarImagem(id);
    }
}
