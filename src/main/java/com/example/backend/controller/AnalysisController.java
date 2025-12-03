package com.example.backend.controller;

import com.example.backend.dto.AnalysisDTO;
import com.example.backend.exception.ServiceUnavailableException;
import com.example.backend.model.AnalysisModel;
import com.example.backend.service.AnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analyses")
@CrossOrigin(origins = "*")
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    // LISTA TUDO COMO DTO (imagem em Base64)
    @GetMapping
    public List<AnalysisDTO> listAll() {
        return service.listAll();
    }

    // BUSCA POR ID (DTO simplificado)
    @GetMapping("/{id}")
    public ResponseEntity<AnalysisDTO> findById(@PathVariable Long id) {
        AnalysisDTO dto = service.findAnalysis(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    // UPLOAD (CREATE)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
            @RequestParam(value = "device", required = false) String device,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        if (device == null || device.isBlank()) {
            return ResponseEntity.badRequest().body("O campo 'device' é obrigatório.");
        }
        if (imageFile == null || imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body("O campo 'image' é obrigatório.");
        }
        try {
            AnalysisModel model = service.saveAnalysis(device, imageFile);
            AnalysisDTO dto = service.findAnalysis(model.getId());
            Map<String, Object> payload = new HashMap<>();
            payload.put("message", "Análise criada com sucesso");
            payload.put("data", dto);
            return ResponseEntity.ok(payload);
        } catch (ServiceUnavailableException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Serviço indisponível");
            error.put("error", e.getMessage());
            return ResponseEntity.status(503).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erro ao processar análise");
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // UPDATE (apenas status, para exemplo)
    @PutMapping("/{id}")
    public ResponseEntity<AnalysisDTO> updateStatus(@PathVariable Long id, @RequestParam boolean status) {
        AnalysisModel model = service.updateStatus(id, status);
        if (model == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.findAnalysis(model.getId()));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean ok = service.deleteAnalysis(id);
        if (!ok) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // BUSCA A FOTO BRUTA
    @GetMapping("/{id}/image")
    public @ResponseBody byte[] getImage(@PathVariable Long id) {
        return service.getImage(id);
    }
}
