package com.example.backend.controller;

import com.example.backend.dto.AnalisesDTO;
import com.example.backend.model.AnalisesModel;
import com.example.backend.service.AnalisesService;
import org.springframework.http.ResponseEntity;
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

    // BUSCA POR ID (DTO simplificado)
    @GetMapping("/{id}")
    public ResponseEntity<AnalisesDTO> buscarPorId(@PathVariable Long id) {
        AnalisesDTO dto = service.buscarAnaliseDTO(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    // UPLOAD (CREATE)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalisesDTO upload(
            @RequestParam("dispositivo") String dispositivo,
            @RequestParam("imagem") MultipartFile foto
    ) throws Exception {
        AnalisesModel model = service.salvarAnalise(dispositivo, foto);
        return service.toAnalisesDTO(model);
    }

    // UPDATE (apenas status, para exemplo)
    @PutMapping("/{id}")
    public ResponseEntity<AnalisesDTO> atualizarStatus(@PathVariable Long id, @RequestParam boolean status) {
        AnalisesModel model = service.atualizarStatus(id, status);
        if (model == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(service.toAnalisesDTO(model));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        boolean ok = service.deletarAnalise(id);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    // BUSCA A FOTO BRUTA
    @GetMapping("/{id}/foto")
    public @ResponseBody byte[] buscarImagem(@PathVariable Long id) {
        return service.buscarImagem(id);
    }
}
