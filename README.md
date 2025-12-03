# Back-End Security FaceID

Backend Java Spring Boot para análise de segurança com reconhecimento facial e detecção de emoções. O sistema recebe imagens de dispositivos, consulta um serviço Python (FastAPI + DeepFace) e persiste os resultados em um banco MySQL.

## 📋 Sumário

- [Requisitos](#requisitos)
- [Arquitetura](#arquitetura)
- [Instalação e Configuração](#instalação-e-configuração)
- [Executando o Projeto](#executando-o-projeto)
- [Endpoints da API](#endpoints-da-api)
- [Como a Imagem é Armazenada](#como-a-imagem-é-armazenada)
- [Integração com Frontend](#integração-com-frontend)
- [Solução de Problemas](#solução-de-problemas)
- [Estrutura do Projeto](#estrutura-do-projeto)

---

## 📦 Requisitos

### Backend (Java)
- **Java 21** (JDK 21)
- **Maven 3.9+**
- **MySQL 8.0+**

### API Python (Análise de Emoções)
- **Python 3.10+**
- **FastAPI**
- **DeepFace**
- **TensorFlow**

---

## 🏗️ Arquitetura

### Entidades (Banco de Dados)

O sistema utiliza 6 entidades JPA interligadas, agora com nomes em inglês para padronização:

1. **AnalysisModel** – Registro principal da análise
2. **DeviceModel** – Metadados do dispositivo (câmera)
3. **ImageModel** – Armazena a foto (LONGBLOB)
4. **EmotionModel** – Emoção detectada pelo DeepFace
5. **ResultModel** – Classificação (Target/Normal)
6. **ProcessingLogModel** – Auditoria das operações

### Fluxo de Dados

```
Frontend → Backend Java (Spring Boot) → API Python (FastAPI + DeepFace) → MySQL
            ↓                               ↓
        Valida serviço                  Analisa emoção
            ↓                               ↓
        Salva imagem                     Retorna JSON
            ↓                               ↓
        Cria entidades ← ← ← ← ← ← ← Recebe resultado
            ↓
        Retorna DTO (Base64)
```

---

## ⚙️ Instalação e Configuração

### 1️⃣ Configurar Banco de Dados

**Crie o arquivo** `src/main/resources/application.properties` com base no template `application.properties.example`:

```properties
spring.application.name=Back-End-Security-FaceID

spring.datasource.url=jdbc:mysql://localhost:3306/security_face_id
spring.datasource.username=root
spring.datasource.password=suasenha

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

> ⚠️ **Importante**: O arquivo `application.properties` está no `.gitignore` para proteger credenciais. Copie o template e preencha com os seus dados.

### 2️⃣ Criar o Banco de Dados

```sql
CREATE DATABASE security_face_id;
USE security_face_id;
```

### 3️⃣ Migrar Estruturas Antigas

Se você já possuía tabelas antigas, verifique se não existe uma coluna `imagem` na tabela `analises`:

```sql
DESCRIBE analises;
ALTER TABLE analises DROP COLUMN imagem;
```

Isso evita o erro `Field 'imagem' doesn't have a default value`.

---

## 🚀 Executando o Projeto

### Backend Java

```powershell
# Compilar o projeto
mvn clean compile

# Executar o servidor
mvn spring-boot:run
```

Servidor disponível em **http://localhost:8080**.

### API Python (Análise de Emoções)

```powershell
cd c:\caminho\para\face-service
.\venv\Scripts\activate
pip install -r requirements.txt  # Ou instale dependências manualmente
python -m uvicorn main:app --reload
```

Servidor disponível em **http://localhost:8000**. O backend Java depende deste serviço estar ativo.

---

## 📡 Endpoints da API

Todos os endpoints agora usam o prefixo `/analyses` e parâmetros em inglês.

### 📤 POST `/analyses/upload`
Envia uma imagem para análise.

**Parâmetros (multipart/form-data):**
- `device` (string, obrigatório) – Nome do dispositivo/câmera
- `image` (file, obrigatório) – Arquivo JPG/PNG

**Resposta de Sucesso (200):**
```json
{
  "message": "Análise criada com sucesso",
  "data": {
    "id": 1,
    "device": "Camera01",
    "status": false,
    "createdAt": "2025-11-26T10:30:00",
    "updatedAt": "2025-11-26T10:30:00",
    "imageBase64": "/9j/4AAQSkZJRg..."
  }
}
```

**Erros comuns:** 400 (campos ausentes), 503 (API Python offline), 500 (erro interno).

**Exemplo cURL:**
```powershell
curl -X POST "http://localhost:8080/analyses/upload" `
  -F "device=Camera01" `
  -F "image=@c:/caminho/para/foto.jpg"
```

---

### 📋 GET `/analyses`
Lista todas as análises retornando DTO com imagem em Base64.

```json
[
  {
    "id": 1,
    "device": "Camera01",
    "status": false,
    "createdAt": "2025-11-26T10:30:00",
    "updatedAt": "2025-11-26T10:30:00",
    "imageBase64": "/9j/4AAQ..."
  }
]
```

---

### 🔍 GET `/analyses/{id}`
Busca análise por ID. Retorna 404 caso não exista.

---

### ✏️ PUT `/analyses/{id}`
Atualiza apenas o campo `status`.

```
PUT /analyses/1?status=true
```

Retorna o DTO atualizado.

---

### 🗑️ DELETE `/analyses/{id}`
Remove uma análise. Retorna 204 em caso de sucesso.

---

### 🖼️ GET `/analyses/{id}/image`
Retorna os bytes crus da imagem (ideal para download direto).

```javascript
fetch('http://localhost:8080/analyses/1/image')
  .then(res => res.blob())
  .then(blob => {
    const url = URL.createObjectURL(blob);
    img.src = url;
  });
```

---

## 💾 Como a Imagem é Armazenada

### Tabela `imagens`
```sql
CREATE TABLE imagens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nome_arquivo VARCHAR(255),
  tamanho BIGINT,
  hash VARCHAR(255),
  dados LONGBLOB
);
```

### Tabela `analises`
```sql
CREATE TABLE analises (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dispositivo_id BIGINT,
  imagem_id BIGINT,
  emocao_id BIGINT,
  resultado_id BIGINT,
  log_id BIGINT,
  status BOOLEAN,
  created_at DATETIME,
  updated_at DATETIME
);
```

### Fluxo de Salvamento

1. Frontend envia `FormData` via `/analyses/upload`.
2. Backend chama a API Python para validar e classificar.
3. Imagem é persistida apenas após a resposta do serviço externo.
4. Cria registros de Device/Image/Emotion/Result/ProcessingLog.
5. Retorna DTO convertendo a imagem para Base64.

### ✅ Vantagens

- Transação atômica (salva tudo ou nada).
- Dados consistentes e auditáveis.
- Sem dependência de storage externo.
- Backup simplificado (dump do banco).

---

## 🌐 Integração com Frontend

### Upload

```javascript
const formData = new FormData();
formData.append('device', 'Camera01');
formData.append('image', fileInput.files[0]);

fetch('http://localhost:8080/analyses/upload', {
  method: 'POST',
  body: formData
})
  .then(res => res.json())
  .then(console.log);
```

### Mostrar Base64

```javascript
fetch('http://localhost:8080/analyses/1')
  .then(res => res.json())
  .then(data => {
    img.src = `data:image/jpeg;base64,${data.imageBase64}`;
  });
```

### Download direto

```javascript
fetch('http://localhost:8080/analyses/1/image')
  .then(res => res.blob())
  .then(blob => {
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'analysis.jpg';
    link.click();
  });
```

---

## 🛠️ Solução de Problemas

### 503 Service Unavailable
- **Causa:** API Python offline.
- **Solução:** iniciar o servidor FastAPI e testar com `curl http://localhost:8000/docs`.

### `Field 'imagem' doesn't have a default value`
- **Causa:** coluna antiga `imagem` ainda existe.
- **Solução:** remover a coluna conforme instruções em [Instalação](#instalação-e-configuração).

### Porta 8080 em uso
- `netstat -ano | findstr :8080`
- `taskkill /F /PID <PID>`

### Imagem não aparece no frontend
- Verifique se `imageBase64` está presente na resposta.
- Garanta o prefixo `data:image/jpeg;base64,`.
- Como alternativa, busque `/analyses/{id}/image`.

---

## 📁 Estrutura do Projeto

```
Back-End-Security-FaceID/
├── src/
│   ├── main/java/com/example/backend/
│   │   ├── controller/AnalysisController.java
│   │   ├── service/
│   │   │   ├── AnalysisService.java
│   │   │   ├── DeviceService.java
│   │   │   ├── ImageStorageService.java
│   │   │   ├── EmotionRecordService.java
│   │   │   ├── ResultRecordService.java
│   │   │   ├── ProcessingLogService.java
│   │   │   └── EmotionApiClient.java
│   │   ├── mapper/AnalysisMapper.java
│   │   ├── dto/AnalysisDTO.java
│   │   ├── model/
│   │   │   ├── AnalysisModel.java
│   │   │   ├── DeviceModel.java
│   │   │   ├── ImageModel.java
│   │   │   ├── EmotionModel.java
│   │   │   ├── ResultModel.java
│   │   │   └── ProcessingLogModel.java
│   │   ├── repository/
│   │   │   ├── AnalysisRepository.java
│   │   │   ├── DeviceRepository.java
│   │   │   ├── ImageRepository.java
│   │   │   ├── EmotionRepository.java
│   │   │   ├── ResultRepository.java
│   │   │   └── ProcessingLogRepository.java
│   │   └── exception/ServiceUnavailableException.java
│   └── resources/application.properties
├── pom.xml
└── README.md
```

---

## 🔐 Segurança

1. **Credenciais:** utilize variáveis de ambiente (`spring.datasource.password=${DB_PASSWORD}`).
2. **CORS:** restrinja origens confiáveis (`@CrossOrigin(origins = "https://seudominio.com")`).
3. **HTTPS:** configure TLS em produção.
4. **Uploads:** limite o tamanho (`spring.servlet.multipart.max-file-size=5MB`).

---

## 📄 Licença

Projeto sob licença MIT.

---

## 👨‍💻 Autor

Desenvolvido por Davi Mancebo.

---

## 🆘 Suporte

Abra uma issue no GitHub em caso de dúvidas ou problemas.
