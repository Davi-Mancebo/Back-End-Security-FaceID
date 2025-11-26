package com.example.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analises")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalisesModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean status; // ✅ ADICIONAR

    @ManyToOne
    @JoinColumn(name = "imagem_id")
    private ImagemModel imagem;

    @ManyToOne
    @JoinColumn(name = "dispositivo_id")
    private DispositivoModel dispositivo;

    @ManyToOne
    @JoinColumn(name = "emocao_id")
    private EmocaoModel emocao;

    @ManyToOne
    @JoinColumn(name = "resultado_id")
    private ResultadoModel resultado;

    @ManyToOne
    @JoinColumn(name = "log_id")
    private LogProcessamentoModel logProcessamento;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
