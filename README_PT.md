# Back-End-Security-FaceID

Este repositório contém um backend Java Spring Boot que aceita imagens faciais de dispositivos, envia para um classificador de emoções Python FastAPI, e persiste os resultados em banco de dados. Inclui entidades para armazenar detalhes da análise: Dispositivo, Imagem, Emoção, Resultado e LogProcessamento.

## Requisitos

- Java 21 (JDK 21)
- Maven 3.9.x
- MySQL configurado via `application.properties` (ou H2 para testes rápidos)
- (Opcional) IDE com plugin Lombok + annotation processing habilitado (IntelliJ, Eclipse, NetBeans, VS Code)
- Python 3.10+ e pacotes para o detector de emoções (se usar o FastAPI fornecido)

## Executando o Backend Java

1. Configure o banco de dados em `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/security_face_id
spring.datasource.username=root
spring.datasource.password=suasenha
```

2. **⚠️ IMPORTANTE - Corrigir Conflito de Coluna**

   Execute no MySQL **antes** de iniciar o backend:
   
   ```sql
   USE security_face_id;
   DESCRIBE analises;
   ```
   
   Se você vir coluna `imagem` (além de `imagem_id`), remova-a:
   
   ```sql
   ALTER TABLE analises DROP COLUMN imagem;
   ```
   
   Isso resolve: `"Field 'imagem' doesn't have a default value"`

3. Execute:

```powershell
.\mvnw spring-boot:run
```

O servidor inicia em `http://localhost:8080`.

## Endpoints Disponíveis

### POST /analises/upload
Upload de imagem para análise (multipart/form-data).

**Parâmetros:**
- `dispositivo` (string) — nome do dispositivo
- `foto` (file) — arquivo de imagem

**Resposta de Sucesso (200):**
```json
{
  "success": true,
  "message": "Análise criada com sucesso",
  "data": {
    "id": 1,
    "dispositivo": "Camera01",
    "status": false,
    "createdAt": "2025-11-25T10:30:00",
    "updatedAt": "2025-11-25T10:30:00",
    "imagemBase64": "..."
  }
}
```

**Erros:**
- 400: Campos ausentes/inválidos
- 500: Erro interno (verificar logs)

### GET /analises
Lista todas as análises (formato DTO simplificado).

### GET /analises/{id}
Busca análise por ID (retorna com imagem Base64).

### PUT /analises/{id}
Atualiza o status de uma análise.

**Body:**
```json
{
  "status": true
}
```

### DELETE /analises/{id}
Deleta uma análise.

### GET /analises/{id}/foto
Retorna os bytes brutos da imagem armazenada (para download direto).

## Exemplo de Uso

```bash
curl -X POST "http://localhost:8080/analises/upload" \
  -F "dispositivo=Camera01" \
  -F "foto=@c:/caminho/para/imagem.jpg"
```

## API Python de Detecção de Emoções

Execute o classificador de emoções (FastAPI) independentemente:

1. Crie virtualenv e instale pacotes:

```bash
python -m venv .venv
.venv\Scripts\activate
pip install fastapi uvicorn pillow deepface tensorflow
```

2. Crie `emotion_api.py`:

```python
from fastapi import FastAPI, UploadFile, File
from deepface import DeepFace
import shutil, os, uuid

app = FastAPI()

TEMP_DIR = 'temp'
os.makedirs(TEMP_DIR, exist_ok=True)

@app.post('/emotion')
async def emotion(image: UploadFile = File(...)):
    img_path = f"{TEMP_DIR}/{uuid.uuid4()}.jpg"
    with open(img_path, 'wb') as f:
        shutil.copyfileobj(image.file, f)
    
    result = DeepFace.analyze(img_path, actions=['emotion'], enforce_detection=False)
    os.remove(img_path)
    
    if isinstance(result, list):
        result = result[0]
    emotion = result.get('dominant_emotion', '').lower()
    target_emotions = ['fear', 'sad', 'angry']
    is_target = emotion in target_emotions
    
    return {"result": is_target, "emotion": emotion}
```

3. Execute:

```bash
uvicorn emotion_api:app --reload --port 8000
```

> Se a FastAPI rodar em host/porta diferente, altere `fastApiUrl` em `AnalisesService`.

## Como a Imagem é Armazenada

### No Banco de Dados (Backend)

1. **Tabela `imagens`**: Imagem salva como `LONGBLOB` no MySQL
   - `dados`: bytes da imagem
   - `nomeArquivo`: nome original do arquivo
   - `tamanho`: tamanho em bytes
   - `hash`: verificação de integridade (opcional)

2. **Tabela `analises`**: Apenas referência
   - `imagem_id`: chave estrangeira apontando para `imagens`
   - Não duplica os bytes (normalização)

### Fluxo de Salvamento

1. Frontend → **POST /analises/upload** com `FormData` (multipart)
2. Backend recebe `MultipartFile` via Spring
3. `AnalisesService.salvarAnalise()`:
   - Converte `MultipartFile` → `byte[]` com `foto.getBytes()`
   - Cria `ImagemModel` e salva no banco (gera ID)
   - Envia bytes para FastAPI Python
   - Recebe emoção detectada
   - Cria entidades relacionadas (Emoção, Resultado, Log)
   - Monta `AnalisesModel` com referências
   - Salva análise completa
4. Retorna DTO com imagem em Base64

### Integração com Frontend

✅ **Funcionará normalmente** - Duas opções:

**Opção 1: Base64 (Recomendado para exibição)**

```javascript
fetch('http://localhost:8080/analises/1')
  .then(res => res.json())
  .then(data => {
    const imgSrc = `data:image/jpeg;base64,${data.data.imagemBase64}`;
    document.getElementById('img').src = imgSrc;
  });
```

**Opção 2: Blob (Para download)**

```javascript
fetch('http://localhost:8080/analises/1/foto')
  .then(res => res.blob())
  .then(blob => {
    const url = URL.createObjectURL(blob);
    document.getElementById('img').src = url;
  });
```

### Vantagens

- ✅ Sem necessidade de servidor de arquivos separado
- ✅ Imagem sempre sincronizada com registro
- ✅ Transações atômicas (salva tudo ou nada)
- ✅ Backup simples (dump do banco)
- ⚠️ Banco cresce rápido (considere limites de tamanho)

## Solução de Problemas

### Erro 500 no Upload

1. **Verificar API Python**: 
   ```bash
   curl http://localhost:8000/docs
   ```

2. **Logs do Backend**: Console onde `mvn spring-boot:run` está rodando

3. **Campos Multipart**: Devem ser `dispositivo` e `foto` (não `imagem`)

4. **Postman**: Remova campo `status` extra (backend define automaticamente)

### "Field 'imagem' doesn't have a default value"

**Causa**: Coluna antiga `imagem` coexistindo com `imagem_id`

**Solução**:
```sql
ALTER TABLE analises DROP COLUMN imagem;
```

### Imagem Não Aparece no Frontend

- Confirme que `imagemBase64` está presente no JSON
- Valide formato Base64 (string sem quebras)
- Teste endpoint alternativo `/analises/{id}/foto`

## Dicas de Desenvolvimento

- **Lombok**: Instale plugin IDE + habilite annotation processing
- **Logs**: Verifique console para stacktraces detalhadas
- **Testes**: Execute com `mvn test`

## Estrutura de Pastas

```
src/main/java/com/example/backend/
├── controller/      # AnalisesController
├── service/         # AnalisesService (lógica de negócio)
├── repository/      # Interfaces JPA
├── model/           # Entidades (AnalisesModel, ImagemModel, etc.)
└── dto/             # AnalisesDTO (resposta simplificada)
```

---

**Próximos Passos Recomendados:**
1. Corrigir coluna `imagem` no banco
2. Testar upload com imagem real
3. Validar integração frontend → backend → FastAPI
4. Adicionar validação de tamanho de arquivo (max 5MB)
5. Implementar paginação em `GET /analises`
