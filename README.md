# Back-End Security FaceID

Backend Java Spring Boot para análise de segurança com reconhecimento facial e detecção de emoções. O sistema recebe imagens de dispositivos, analisa emoções usando DeepFace (Python/FastAPI) e persiste os resultados em banco de dados MySQL.

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
- **MySQL 8.0+** (ou H2 para testes rápidos)
- **Lombok** (incluído no Maven)

### API Python (Análise de Emoções)
- **Python 3.10+**
- **FastAPI**
- **DeepFace**
- **TensorFlow**

---

## 🏗️ Arquitetura

### Entidades (Banco de Dados)

O sistema utiliza 6 entidades JPA interligadas:

1. **AnalisesModel** - Registro principal da análise
2. **DispositivoModel** - Metadados do dispositivo (câmera)
3. **ImagemModel** - Armazena a foto (LONGBLOB)
4. **EmocaoModel** - Emoção detectada pelo DeepFace
5. **ResultadoModel** - Classificação (Alvo/Normal)
6. **LogProcessamentoModel** - Auditoria de operações

### Fluxo de Dados

```
Frontend → Backend Java (Spring Boot) → API Python (FastAPI + DeepFace) → MySQL
				↓                               ↓
		   Valida Python                 Analisa Emoção
				↓                               ↓
		   Salva Imagem                   Retorna JSON
				↓                               ↓
		   Cria Entidades ← ← ← ← ← ← ← Recebe Resultado
				↓
		   Retorna DTO (Base64)
```

---

## ⚙️ Instalação e Configuração

### 1️⃣ Configurar Banco de Dados

Edite `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/security_face_id
spring.datasource.username=root
spring.datasource.password=suasenha

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 2️⃣ Criar o Banco de Dados

```sql
CREATE DATABASE security_face_id;
USE security_face_id;
```

### 3️⃣ ⚠️ IMPORTANTE - Resolver Conflito de Coluna

Se você migrou de uma versão anterior, execute:

```sql
DESCRIBE analises;
```

Se aparecer uma coluna `imagem` (além de `imagem_id`), remova-a:

```sql
ALTER TABLE analises DROP COLUMN imagem;
```

Isso resolve o erro: `"Field 'imagem' doesn't have a default value"`

---

## 🚀 Executando o Projeto

### Backend Java

```powershell
# Compilar o projeto
mvn clean compile

# Executar o servidor
mvn spring-boot:run
```

Servidor disponível em: **http://localhost:8080**

### API Python (Servidor de Análise de Emoções)

Navegue até o repositório da API Python e execute:

```powershell
cd c:\caminho\para\face-service

# Ativar ambiente virtual
.\venv\Scripts\activate

# Instalar dependências (primeira vez)
pip install fastapi uvicorn deepface tensorflow opencv-python python-multipart

# Executar servidor
python -m uvicorn main:app --reload
```

Servidor disponível em: **http://localhost:8000**

> ⚠️ **O backend Java PRECISA que a API Python esteja rodando** para funcionar corretamente!

---

## 📡 Endpoints da API

### 📤 POST `/analises/upload`
Faz upload de uma imagem para análise.

**Parâmetros (multipart/form-data):**
- `dispositivo` (string, obrigatório) - Nome do dispositivo/câmera
- `imagem` (file, obrigatório) - Arquivo de imagem (JPG, PNG, etc.)

**Resposta de Sucesso (200):**
```json
{
  "message": "Análise criada com sucesso",
  "data": {
	"id": 1,
	"dispositivo": "Camera01",
	"status": false,
	"createdAt": "2025-11-26T10:30:00",
	"updatedAt": "2025-11-26T10:30:00",
	"imagemBase64": "/9j/4AAQSkZJRg..."
  }
}
```

**Códigos de Erro:**
- **400 Bad Request** - Campo obrigatório ausente
- **503 Service Unavailable** - API Python offline
- **500 Internal Server Error** - Erro interno no backend

**Exemplo com cURL:**
```powershell
curl -X POST "http://localhost:8080/analises/upload" `
  -F "dispositivo=Camera01" `
  -F "imagem=@c:/caminho/para/foto.jpg"
```

---

### 📋 GET `/analises`
Lista todas as análises (com imagem em Base64).

**Resposta (200):**
```json
[
  {
	"id": 1,
	"dispositivo": "Camera01",
	"status": false,
	"createdAt": "2025-11-26T10:30:00",
	"updatedAt": "2025-11-26T10:30:00",
	"imagemBase64": "/9j/4AAQ..."
  }
]
```

---

### 🔍 GET `/analises/{id}`
Busca análise específica por ID.

**Resposta (200):**
```json
{
  "id": 1,
  "dispositivo": "Camera01",
  "status": false,
  "createdAt": "2025-11-26T10:30:00",
  "updatedAt": "2025-11-26T10:30:00",
  "imagemBase64": "/9j/4AAQ..."
}
```

**Erro (404):** Análise não encontrada

---

### ✏️ PUT `/analises/{id}`
Atualiza o status de uma análise.

**Query Parameter:**
- `status` (boolean) - Novo status (true/false)

**Exemplo:**
```
PUT /analises/1?status=true
```

**Resposta (200):** DTO da análise atualizada

---

### 🗑️ DELETE `/analises/{id}`
Deleta uma análise.

**Resposta:**
- **204 No Content** - Sucesso
- **404 Not Found** - Análise não encontrada

---

### 🖼️ GET `/analises/{id}/foto`
Retorna os bytes brutos da imagem (para download direto).

**Resposta:** Bytes da imagem (Content-Type: image/jpeg)

**Exemplo JavaScript:**
```javascript
fetch('http://localhost:8080/analises/1/foto')
  .then(res => res.blob())
  .then(blob => {
	const url = URL.createObjectURL(blob);
	img.src = url;
  });
```

---

## 💾 Como a Imagem é Armazenada

### No Banco de Dados

#### Tabela `imagens`
```sql
CREATE TABLE imagens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  nome_arquivo VARCHAR(255),
  tamanho BIGINT,
  hash VARCHAR(255),
  dados LONGBLOB  -- ← Imagem armazenada aqui
);
```

#### Tabela `analises`
```sql
CREATE TABLE analises (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dispositivo_id BIGINT,
  imagem_id BIGINT,      -- ← Referência, não duplica bytes
  emocao_id BIGINT,
  resultado_id BIGINT,
  log_id BIGINT,
  status BOOLEAN,
  created_at DATETIME,
  updated_at DATETIME
);
```

### Fluxo de Salvamento

1. Frontend envia `FormData` com multipart/form-data
2. Backend valida se API Python está online
3. Envia bytes da imagem para análise
4. Recebe emoção detectada
5. **Só então** salva imagem no banco (LONGBLOB)
6. Cria entidades relacionadas
7. Retorna DTO com imagem em Base64

### ✅ Vantagens

- ✅ Não precisa de servidor de arquivos separado (S3, CDN)
- ✅ Transação atômica (salva tudo ou nada)
- ✅ Imagem sempre sincronizada com registro
- ✅ Backup simples (dump do banco)
- ⚠️ Banco cresce rapidamente (considere limite de tamanho)

---

## 🌐 Integração com Frontend

### Enviar Imagem (Upload)

```javascript
const formData = new FormData();
formData.append('dispositivo', 'Camera01');
formData.append('imagem', fileBlob); // File do input type="file"

fetch('http://localhost:8080/analises/upload', {
  method: 'POST',
  body: formData
})
.then(res => res.json())
.then(data => {
  if (data.message) {
	console.log('Sucesso:', data.data);
  }
});
```

### Exibir Imagem (Base64)

```javascript
fetch('http://localhost:8080/analises/1')
  .then(res => res.json())
  .then(data => {
	// Exibe imagem direto do Base64
	const img = document.getElementById('imgPreview');
	img.src = `data:image/jpeg;base64,${data.imagemBase64}`;
  });
```

### Baixar Imagem (Blob)

```javascript
fetch('http://localhost:8080/analises/1/foto')
  .then(res => res.blob())
  .then(blob => {
	const url = URL.createObjectURL(blob);
	const link = document.createElement('a');
	link.href = url;
	link.download = 'analise.jpg';
	link.click();
  });
```

---

## 🛠️ Solução de Problemas

### Erro 503: "Serviço indisponível"

**Causa:** API Python não está rodando

**Solução:**
```powershell
cd face-service
.\venv\Scripts\activate
python -m uvicorn main:app --reload
```

Verifique se está acessível:
```powershell
curl http://localhost:8000/docs
```

---

### Erro: "Field 'imagem' doesn't have a default value"

**Causa:** Coluna antiga `imagem` coexistindo com `imagem_id`

**Solução:**
```sql
USE security_face_id;
ALTER TABLE analises DROP COLUMN imagem;
```

---

### Erro: "Port 8080 already in use"

**Causa:** Servidor Java já está rodando

**Solução:**
```powershell
# Encontrar processo
netstat -ano | findstr :8080

# Matar processo (substitua <PID>)
taskkill /F /PID <PID>
```

---

### Erro de Lombok na IDE

**Causa:** Annotation processing desabilitado

**Solução (VS Code):**
1. Instale extensão "Language Support for Java"
2. Configure Java → Annotation Processing → Enable

**Solução (IntelliJ):**
1. File → Settings → Build → Compiler → Annotation Processors
2. Marque "Enable annotation processing"

---

### Imagem Não Aparece no Frontend

**Verificações:**
1. ✅ `imagemBase64` está presente no JSON?
2. ✅ String Base64 está completa (sem quebras)?
3. ✅ Prefixo `data:image/jpeg;base64,` adicionado?

**Teste alternativo:**
```javascript
// Use endpoint /foto se Base64 falhar
fetch('/analises/1/foto').then(res => res.blob())...
```

---

## 📁 Estrutura do Projeto

```
Back-End-Security-FaceID/
├── src/
│   ├── main/
│   │   ├── java/com/example/backend/
│   │   │   ├── controller/
│   │   │   │   └── AnalisesController.java      # Endpoints REST
│   │   │   ├── service/
│   │   │   │   └── AnalisesService.java         # Lógica de negócio
│   │   │   ├── repository/
│   │   │   │   ├── AnalisesRepository.java      # JPA Repositories
│   │   │   │   ├── DispositivoRepository.java
│   │   │   │   ├── ImagemRepository.java
│   │   │   │   ├── EmocaoRepository.java
│   │   │   │   ├── ResultadoRepository.java
│   │   │   │   └── LogProcessamentoRepository.java
│   │   │   ├── model/
│   │   │   │   ├── AnalisesModel.java           # Entidades JPA
│   │   │   │   ├── DispositivoModel.java
│   │   │   │   ├── ImagemModel.java
│   │   │   │   ├── EmocaoModel.java
│   │   │   │   ├── ResultadoModel.java
│   │   │   │   └── LogProcessamentoModel.java
│   │   │   ├── dto/
│   │   │   │   └── AnalisesDTO.java             # DTO para resposta
│   │   │   ├── exception/
│   │   │   │   └── ServiceUnavailableException.java
│   │   │   └── BackendSecurityFaceIdApplication.java
│   │   └── resources/
│   │       └── application.properties            # Configurações
│   └── test/
├── target/                                       # Build artifacts
├── pom.xml                                       # Maven dependencies
└── README.md                                     # Este arquivo
```

---

## 🧪 Testes

```powershell
# Executar testes unitários
mvn test

# Executar com cobertura
mvn clean test jacoco:report
```

---

## 🔐 Segurança

### Recomendações para Produção

1. **Senhas**: Use variáveis de ambiente
   ```properties
   spring.datasource.password=${DB_PASSWORD}
   ```

2. **CORS**: Restrinja origens permitidas
   ```java
   @CrossOrigin(origins = "https://seudominio.com")
   ```

3. **HTTPS**: Configure SSL/TLS

4. **Validação**: Adicione limite de tamanho de arquivo
   ```properties
   spring.servlet.multipart.max-file-size=5MB
   ```

---

## 📈 Melhorias Futuras

- [ ] Paginação em `GET /analises`
- [ ] Autenticação JWT
- [ ] Cache com Redis
- [ ] Compressão de imagens
- [ ] Webhooks para notificações
- [ ] Dashboard de métricas

---

## 📝 Notas Técnicas

- **Lombok**: Gera getters/setters via annotation processor do Maven
- **JPA**: `@PrePersist` e `@PreUpdate` gerenciam timestamps automaticamente
- **Transações**: Service valida Python antes de persistir (atomicidade)
- **Exceções**: `ServiceUnavailableException` customizada para 503

---

## 📄 Licença

Este projeto está sob a licença MIT.

---

## 👨‍💻 Autor

Desenvolvido por Davi Mancebo

---

## 🆘 Suporte

Problemas ou dúvidas? Abra uma issue no repositório do GitHub.
