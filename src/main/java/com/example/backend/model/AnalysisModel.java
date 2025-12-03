package com.example.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analises")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean status;

    @ManyToOne
    @JoinColumn(name = "imagem_id")
    private ImageModel image;

    @ManyToOne
    @JoinColumn(name = "dispositivo_id")
    private DeviceModel device;

    @ManyToOne
    @JoinColumn(name = "emocao_id")
    private EmotionModel emotion;

    @ManyToOne
    @JoinColumn(name = "resultado_id")
    private ResultModel result;

    @ManyToOne
    @JoinColumn(name = "log_id")
    private ProcessingLogModel processingLog;

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
