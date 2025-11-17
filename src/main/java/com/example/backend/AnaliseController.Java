package com.example.backend.controller;

import com.example.backend.src.model.Analise;
import com.example.backend.src.service.AnaliseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analises")
public class AnaliseController {

    private final AnaliseService service;

    public AnaliseController(AnaliseService service) {
        this.service = service;
    }

    @PostMapping
    public Analise criar(@RequestBody Analise analise) {
        return service.criar(analise);
    }

    @GetMapping
    public List<Analise> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Analise buscar(@PathVariable Long id) {
        return service.buscarPorId(id);
    }
}