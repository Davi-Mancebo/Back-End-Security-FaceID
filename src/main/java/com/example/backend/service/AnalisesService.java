package com.example.backend.service;

import com.example.backend.dto.AnalisesDTO;
import com.example.backend.model.*;
import com.example.backend.repository.*;
import com.example.backend.exception.ServiceUnavailableException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.Base64;
import java.util.logging.Logger;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class AnalisesService {
    private static final Logger LOGGER = Logger.getLogger(AnalisesService.class.getName());
    private final AnalisesRepository analisesRepository;
    private final DispositivoRepository dispositivoRepository;
    private final ImagemRepository imagemRepository;
    private final EmocaoRepository emocaoRepository;
    private final ResultadoRepository resultadoRepository;
    private final LogProcessamentoRepository logProcessamentoRepository;

    public AnalisesService(
        AnalisesRepository analisesRepository,
        DispositivoRepository dispositivoRepository,
        ImagemRepository imagemRepository,
        EmocaoRepository emocaoRepository,
        ResultadoRepository resultadoRepository,
        LogProcessamentoRepository logProcessamentoRepository
    ) {
        this.analisesRepository = analisesRepository;
        this.dispositivoRepository = dispositivoRepository;
        this.imagemRepository = imagemRepository;
        this.emocaoRepository = emocaoRepository;
        this.resultadoRepository = resultadoRepository;
        this.logProcessamentoRepository = logProcessamentoRepository;
    }

    public AnalisesModel salvarAnalise(String dispositivoNome, MultipartFile foto) throws Exception {
        try {
            // 1. Dispositivo
            DispositivoModel dispositivo = dispositivoRepository.findByNome(dispositivoNome);
            if (dispositivo == null) {
                dispositivo = new DispositivoModel();
                dispositivo.setNome(dispositivoNome);
                dispositivo.setTipo("Desconhecido");
                dispositivo.setLocalizacao("");
                dispositivo = dispositivoRepository.save(dispositivo);
            }

            // 2. Prepara imagem MAS NÃO SALVA AINDA
            byte[] imageBytes = foto.getBytes();

            // 3. CHAMA API PYTHON PRIMEIRO (validação antes de persistir)
            EmotionApiResult emotionResult = callEmotionApiFull(imageBytes);
            if (emotionResult == null) {
                throw new ServiceUnavailableException("API de análise de emoções está indisponível. Verifique se o servidor Python está rodando.");
            }
            if (emotionResult.emotion == null || emotionResult.emotion.isEmpty()) {
                throw new RuntimeException("API Python retornou dados inválidos");
            }

            // 4. SÓ AGORA salva a imagem (após confirmar que Python está ok)
            ImagemModel imagem = new ImagemModel();
            imagem.setNomeArquivo(foto.getOriginalFilename());
            imagem.setTamanho(foto.getSize());
            imagem.setHash("");
            imagem.setDados(imageBytes);
            imagem = imagemRepository.save(imagem);

            // 4. Emoção
            EmocaoModel emocao = new EmocaoModel();
            emocao.setNome(emotionResult.emotion);
            emocao.setScore(null); // não vem score da API
            emocao.setDataHora(java.time.LocalDateTime.now());
            emocao = emocaoRepository.save(emocao);

            // 5. Resultado
            ResultadoModel resultado = new ResultadoModel();
            resultado.setResultado(emotionResult.isTarget ? "Alvo" : "Normal");
            resultado.setDetalhes("Emoção dominante: " + emotionResult.emotion);
            resultado = resultadoRepository.save(resultado);

            // 6. Log de processamento
            LogProcessamentoModel log = new LogProcessamentoModel();
            log.setDataHora(java.time.LocalDateTime.now());
            log.setStatus("OK");
            log.setMensagem("Análise criada com sucesso");
            log = logProcessamentoRepository.save(log);

            // 7. Monta AnalisesModel
            AnalisesModel model = new AnalisesModel();
            model.setDispositivo(dispositivo);
            model.setImagem(imagem);
            model.setEmocao(emocao);
            model.setResultado(resultado);
            model.setLogProcessamento(log);
            model.setStatus(emotionResult.isTarget);
            return analisesRepository.save(model);
        } catch (Exception e) {
            LOGGER.severe("Erro ao salvar análise: " + e.getMessage());
            // tenta salvar log independente, se possivel
            try {
                LogProcessamentoModel errorLog = new LogProcessamentoModel();
                errorLog.setDataHora(java.time.LocalDateTime.now());
                errorLog.setStatus("ERRO");
                errorLog.setMensagem(e.getMessage());
                logProcessamentoRepository.save(errorLog);
            } catch (Exception ex) {
                LOGGER.warning("Erro ao salvar log de erro: " + ex.getMessage());
            }
            throw new Exception("Erro ao salvar análise: " + e.getMessage(), e);
        }
    }

    // Classe auxiliar para resposta da API Python
    private static class EmotionApiResult {
        public boolean isTarget;
        public String emotion;
    }

    // Chama API Python e retorna emoção e isTarget
    private EmotionApiResult callEmotionApiFull(byte[] imageBytes) {
        String fastApiUrl = "http://localhost:8000/emotion";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        Resource fileResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "image.jpg";
            }
        };

        org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("image", fileResource);

        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        EmotionApiResult result = new EmotionApiResult();
        result.isTarget = false;
        result.emotion = "";
        try {
            ResponseEntity<java.util.Map> response = restTemplate.postForEntity(fastApiUrl, requestEntity, java.util.Map.class);
            java.util.Map resp = response.getBody();
            if (resp != null) {
                Object isTarget = resp.get("result");
                Object emotion = resp.get("emotion");
                if (isTarget instanceof Boolean) result.isTarget = (Boolean) isTarget;
                if (emotion instanceof String) result.emotion = (String) emotion;
            }
            return result;
        } catch (Exception e) {
            LOGGER.severe("Erro ao comunicar com API Python: " + e.getMessage());
            return null; // Retorna null para indicar falha na comunicação
        }
    }

    private boolean callEmotionApi(byte[] imageBytes) {
        String fastApiUrl = "http://localhost:8000/emotion"; // Change if needed
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        Resource fileResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "image.jpg";
            }
        };

        org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("image", fileResource);

        HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<java.util.Map> response = restTemplate.postForEntity(fastApiUrl, requestEntity, java.util.Map.class);
            Object result = response.getBody().get("result");
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        } catch (Exception e) {
            // Log or handle error as needed
        }
        return false; // Default if error
    }

    public byte[] buscarImagem(Long id) {
        return analisesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro não encontrado"))
                .getImagem().getDados();
    }

    public List<AnalisesDTO> listarTudoDTO() {
        return analisesRepository.findAll().stream().map(this::toAnalisesDTO).collect(Collectors.toList());
    }

    public AnalisesDTO buscarAnaliseDTO(Long id) {
        return analisesRepository.findById(id).map(this::toAnalisesDTO).orElse(null);
    }

    public AnalisesDTO toAnalisesDTO(AnalisesModel model) {
        AnalisesDTO dto = new AnalisesDTO();
        dto.setId(model.getId());
        dto.setDispositivo(model.getDispositivo() != null ? model.getDispositivo().getNome() : null);
        dto.setStatus(model.isStatus());
        dto.setCreatedAt(model.getCreatedAt());
        dto.setUpdatedAt(model.getUpdatedAt());
        dto.setImagemBase64(model.getImagem() != null ? Base64.getEncoder().encodeToString(model.getImagem().getDados()) : null);
        return dto;
    }

    public AnalisesModel atualizarStatus(Long id, boolean status) {
        return analisesRepository.findById(id).map(model -> {
            model.setStatus(status);
            return analisesRepository.save(model);
        }).orElse(null);
    }

    public boolean deletarAnalise(Long id) {
        if (!analisesRepository.existsById(id)) return false;
        analisesRepository.deleteById(id);
        return true;
    }
}
