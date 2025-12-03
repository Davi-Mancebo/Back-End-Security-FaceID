package com.example.backend.service;

import com.example.backend.dto.AnalysisDTO;
import com.example.backend.exception.ServiceUnavailableException;
import com.example.backend.mapper.AnalysisMapper;
import com.example.backend.model.*;
import com.example.backend.repository.AnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.logging.Logger;

@Service
public class AnalysisService {

    private static final Logger LOGGER = Logger.getLogger(AnalysisService.class.getName());

    private final AnalysisRepository analysisRepository;
    private final DeviceService deviceService;
    private final ImageStorageService imageStorageService;
    private final EmotionRecordService emotionRecordService;
    private final ResultRecordService resultRecordService;
    private final ProcessingLogService processingLogService;
    private final EmotionApiClient emotionApiClient;
    private final AnalysisMapper analysisMapper;

    public AnalysisService(AnalysisRepository analysisRepository,
                           DeviceService deviceService,
                           ImageStorageService imageStorageService,
                           EmotionRecordService emotionRecordService,
                           ResultRecordService resultRecordService,
                           ProcessingLogService processingLogService,
                           EmotionApiClient emotionApiClient,
                           AnalysisMapper analysisMapper) {
        this.analysisRepository = analysisRepository;
        this.deviceService = deviceService;
        this.imageStorageService = imageStorageService;
        this.emotionRecordService = emotionRecordService;
        this.resultRecordService = resultRecordService;
        this.processingLogService = processingLogService;
        this.emotionApiClient = emotionApiClient;
        this.analysisMapper = analysisMapper;
    }

    /**
     * Cria uma nova análise validando primeiro a API Python para garantir consistência.
     */
    public AnalysisModel saveAnalysis(String deviceName, MultipartFile imageFile) throws Exception {
        try {
            DeviceModel device = deviceService.findOrCreate(deviceName);
            byte[] imageBytes = imageFile.getBytes();

            EmotionApiClient.EmotionApiResponse apiResponse = emotionApiClient.analyze(imageBytes);

            ImageModel image = imageStorageService.storeImage(imageFile.getOriginalFilename(), imageFile.getSize(), imageBytes);
            EmotionModel emotion = emotionRecordService.saveEmotion(apiResponse.emotion());
            ResultModel result = resultRecordService.saveResult(apiResponse);
            ProcessingLogModel log = processingLogService.registerSuccess("Análise criada com sucesso");

            AnalysisModel analysis = new AnalysisModel();
            analysis.setDevice(device);
            analysis.setImage(image);
            analysis.setEmotion(emotion);
            analysis.setResult(result);
            analysis.setProcessingLog(log);
            analysis.setStatus(apiResponse.target());
            return analysisRepository.save(analysis);
        } catch (ServiceUnavailableException e) {
            processingLogService.registerFailure(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.severe("Erro ao salvar análise: " + e.getMessage());
            processingLogService.registerFailure(e.getMessage());
            throw new Exception("Erro ao salvar análise: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera a imagem em bytes para download direto.
     */
    public byte[] getImage(Long id) {
        return analysisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"))
                .getImage().getData();
    }

    public List<AnalysisDTO> listAll() {
        return analysisRepository.findAll().stream().map(analysisMapper::toDto).toList();
    }

    public AnalysisDTO findAnalysis(Long id) {
        return analysisRepository.findById(id).map(analysisMapper::toDto).orElse(null);
    }

    public AnalysisModel updateStatus(Long id, boolean status) {
        return analysisRepository.findById(id).map(analysis -> {
            analysis.setStatus(status);
            return analysisRepository.save(analysis);
        }).orElse(null);
    }

    public boolean deleteAnalysis(Long id) {
        if (!analysisRepository.existsById(id)) {
            return false;
        }
        analysisRepository.deleteById(id);
        return true;
    }
}
