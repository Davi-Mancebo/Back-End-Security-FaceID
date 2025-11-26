# Back-End-Security-FaceID

This repository contains a Java Spring Boot backend that accepts face images from devices, sends them to a Python FastAPI emotion classifier, and persists results to a database. It now also contains extra entities to store details per analysis: Dispositivo (device), Imagem (image), Emocao (emotion), Resultado (result) and LogProcessamento (processing log).

## Requirements

- Java 21 (JDK 21)
- Maven 3.9.x
- MySQL or other DB configured via `application.properties` (or you can use H2 for quick testing)
- (Optional) IDE with Lombok plugin + annotation processing enabled (IntelliJ, Eclipse, NetBeans, VS Code). Lombok is used for getter/setter/constructors.
- Python 3.10+ and packages for the emotion detector (if using the provided FastAPI)

## Running the Java backend

1. Make sure your database is set in `src/main/resources/application.properties` (or use default settings).

2. Build and run:

```powershell
# Build and run with maven wrapper
.\.\mvnw spring-boot:run
```

The server will start at `http://localhost:8080` by default.

## Example endpoints

- POST /analises/upload (multipart/form-data) — parameters:
	- `dispositivo` (string) — device name
	- `imagem` (file) — the image

Returns: 200 OK with the DTO containing id, dispositivo, status, imagem (base64). If incorrect or missing fields, returns 400. If internal error, returns 500.

- GET /analises — list all analyses (simple DTO)
- GET /analises/{id} — get analysis by id
- PUT /analises/{id}?status=true|false — update status (example)
- DELETE /analises/{id} — delete an analysis
- GET /analises/{id}/foto — returns the raw bytes of the stored photo

### Example curl (upload)

```bash
curl -X POST "http://localhost:8080/analises/upload" \
	-H "Content-Type: multipart/form-data" \
	-F "dispositivo=mac-b" \
	-F "imagem=@/path/to/image.jpg"
```

If you receive HTTP 500 on upload:
- Check the backend logs for stacktrace (in the console where `mvn spring-boot:run` is running).
- Ensure Python FastAPI is running and accessible at the configured URL (default: `http://localhost:8000/emotion`).
- Confirm the multipart form fields include `dispositivo` and `imagem`.
- If you are using Postman, remove any extra `status` key (the backend sets status automatically using the FastAPI analyzer).

## Python FastAPI emotion detector (example)

You can run the Emotion classifier API (FastAPI) independently. A minimal example:

1. Create virtualenv and install packages

```bash
python -m venv .venv
.venv\Scripts\activate    # On Windows
pip install fastapi uvicorn pillow deepface numpy
```

2. Example FastAPI app (file `emotion_api.py`):

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

		result = DeepFace.analyze(img_path, actions=['emotion'])
		os.remove(img_path)

		if isinstance(result, list):
				result = result[0]
		emotion = result.get('dominant_emotion', '').lower()
		target_emotions = ['fear', 'sad', 'angry', 'surprise']
		is_target = any(e in emotion for e in target_emotions)

		# Return as {result: bool, emotion: string}
		return {"result": is_target, "emotion": emotion}
```

Run with:

```bash
uvicorn emotion_api:app --reload --port 8000
```

> If you run the FastAPI on a different host/port, change the `fastApiUrl` variable in `AnalisesService`.

## Development / Debugging tips

- If you see Lombok-related errors in the IDE, install Lombok plugin and enable annotation processing and reload the IDE.
- If you see a 500 Internal Server Error for `POST /analises/upload`, check:
	- that the FastAPI is running and reachable (`http://localhost:8000/emotion`)
	- check backend logs for stacktrace (we print error messages to console)
	- make sure multipart form used by the client includes `dispositivo` and `imagem` fields.

## Tests

To run backend unit tests (if you add some):

```bash
mvn -U test
```

## Notes

- The project uses Lombok for boilerplate code (getters/setters). The Maven build includes Lombok as an annotation processor.
- If you want stricter error handling, we can return structured JSON responses with code/message payloads and use a global exception handler (ControllerAdvice).

---

If you want, I can:
- add example Postman collection
- switch to H2 in memory DB for easier dev setup
- return a structured success response for upload endpoint
- create a small unit test that calls the service