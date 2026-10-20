<p align="center">
  <img src="https://raw.githubusercontent.com/ferranpons/llamatik/main/assets/llamatik-new-logo.png" alt="Llamatik Logo" width="150"/>
</p>

<h1 align="center">Llamatik Server</h1>

<p align="center">
  <b>Self-hosted inference server fully compatible with the Llamatik library API.</b>
</p>

<p align="center">
  Self-hosted · Full API parity · Zero app-code changes
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-JVM-blueviolet" alt="kotlin badge"/>
  <img src="https://img.shields.io/badge/Ktor-3.3-ff4088" alt="ktor badge"/>
  <img src="https://img.shields.io/badge/Llamatik-2.1.0-orange" alt="llamatik badge"/>
  <img src="https://img.shields.io/badge/LLM-llama.cpp-orange" alt="llama badge"/>
  <img src="https://img.shields.io/badge/STT-whisper.cpp-blue" alt="whisper badge"/>
  <img src="https://img.shields.io/badge/Image-stablediffusion.cpp-purple" alt="sd badge"/>
  <img src="https://img.shields.io/badge/License-MIT-lightgrey" alt="license badge"/>
  <img src="https://img.shields.io/github/actions/workflow/status/ferranpons/Llamatik-Server/ci.yml?label=CI" alt="ci badge"/>
</p>

---

## ✨ What is Llamatik Server?

**Llamatik Server** is a self-hosted HTTP backend that exposes the **same API as the [Llamatik Kotlin library](https://github.com/ferranpons/Llamatik)**. Deploy it on any machine and point your app at it — no client-side code changes required.

It covers the full `LlamaBridge`, `WhisperBridge`, `StableDiffusionBridge`, and `MultimodalBridge` surface: LLM generation, embeddings, speech-to-text, image generation, and multimodal vision — all over HTTP.

---

## 🚀 Features

- ✅ Full parity with **Llamatik core 2.1.0** API
- ✅ LLM inference via **llama.cpp** — streaming & non-streaming
- ✅ **Schema-constrained JSON generation**
- ✅ **Embeddings** for vector search & RAG
- ✅ **KV session management** — save, load, reset, continue, streaming continue
- ✅ **Chat template** introspection and rendering
- ✅ **Speculative decoding (MTP)** — multi-token prediction head for faster throughput
- ✅ **GPU layer offload** — configurable via `gpuLayers` parameter
- ✅ **Speech-to-Text** via whisper.cpp (`/v1/whisper`) — flat transcription and segment-aware with timestamps & diarization
- ✅ **Image generation** via stable-diffusion.cpp (`/v1/stablediffusion`)
- ✅ **Multimodal / VLM** image analysis via streaming SSE (`/v1/multimodal`)
- ✅ Production-ready **Ktor** server (Apache Tomcat Jakarta)
- ✅ JWT authentication + user profiles
- ✅ Docker-ready deployment
- ✅ CI via GitHub Actions — tests run on every PR

---

## 🛠 Requirements

- JVM **21+**
- Docker (optional, for containerised deployment)

---

## ▶️ Running Locally

```bash
./gradlew run
```

The server starts on:

```
http://localhost:8080
```

---

## 🐳 Running with Docker

Build the image:

```bash
docker build -t llamatik-server .
```

Run the container:

```bash
docker run -p 8080:8080 llamatik-server
```

---

## 🖥 Running as a Linux Service (systemd)

Create `/etc/systemd/system/docker.llamatik.service`:

```ini
[Unit]
Description=Llamatik Server
After=docker.service
Requires=docker.service

[Service]
TimeoutStartSec=0
Restart=always
ExecStartPre=-/usr/bin/docker exec %n stop
ExecStartPre=-/usr/bin/docker rm %n
ExecStart=/usr/bin/docker run -p 8080:8080 llamatik-server

[Install]
WantedBy=default.target
```

Enable on boot:

```bash
sudo systemctl enable docker.llamatik
```

---

## 🌍 Environment Variables

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | ✅ | HMAC-512 secret for JWT signing |
| `JDBC_DRIVER` | ✅ | e.g. `org.postgresql.Driver` |
| `JDBC_DATABASE_URL` | ✅ | Full JDBC connection URL |
| `DB_USER` | | Database username |
| `DB_PASSWORD` | | Database password |
| `DB_MAX_POOL_SIZE` | | HikariCP pool size (default: `3`) |
| `ENABLE_SHUTDOWN_URL` | | Set to `"true"` to enable `POST /ktor/application/shutdown` |

---

## 📡 API Reference

All endpoints are prefixed with `/v1`.

---

### 🧠 LLM — Embeddings (`/v1/embeddings`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/embeddings/init` | Load an embedding model |
| POST | `/v1/embeddings/embed` | Compute an embedding vector |

#### `POST /v1/embeddings/init`
```json
{ "modelPath": "/models/nomic-embed.gguf" }
```
```json
{ "ok": true }
```

#### `POST /v1/embeddings/embed`
```json
{ "input": "Kotlin Multiplatform" }
```
```json
{ "embedding": [0.12, -0.04, 0.87, "..."] }
```

---

### 🧠 LLM — Generation (`/v1/generation`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/generation/init` | Load a generation model |
| POST | `/v1/generation/generate` | One-shot generation |
| POST | `/v1/generation/generateWithContext` | Generation with system + context |
| POST | `/v1/generation/generateJson` | JSON-constrained generation |
| POST | `/v1/generation/generateJsonWithContext` | JSON generation with context |
| POST | `/v1/generation/continue` | Continue from existing KV cache |
| POST | `/v1/generation/stream` | Streaming generation (SSE) |
| POST | `/v1/generation/streamWithContext` | Streaming with context (SSE) |
| POST | `/v1/generation/jsonStream` | Streaming JSON generation (SSE) |
| POST | `/v1/generation/jsonStreamWithContext` | Streaming JSON with context (SSE) |
| POST | `/v1/generation/continueStream` | Streaming continue from KV cache (SSE) |
| POST | `/v1/generation/params` | Update generation parameters |
| POST | `/v1/generation/cancel` | Cancel ongoing generation |
| POST | `/v1/generation/shutdown` | Unload model and free native resources |
| POST | `/v1/generation/mtp/init` | Initialize speculative decoding (MTP head) |
| POST | `/v1/generation/mtp/shutdown` | Release MTP resources |

#### `POST /v1/generation/generate`
```json
{ "prompt": "Explain Kotlin Multiplatform in one sentence." }
```
```json
{ "text": "Kotlin Multiplatform lets you share business logic across Android, iOS, and Desktop from a single codebase." }
```

#### `POST /v1/generation/generateWithContext`
```json
{
  "systemPrompt": "You are a helpful assistant.",
  "contextBlock": "The user is building a KMP app.",
  "userPrompt": "Which libraries should I use?"
}
```

#### `POST /v1/generation/generateJson`
```json
{
  "prompt": "Extract the city and country from: I live in Barcelona, Spain.",
  "jsonSchema": "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"},\"country\":{\"type\":\"string\"}}}"
}
```

#### `POST /v1/generation/params`

All fields except the first five are optional and default to sensible values.

```json
{
  "temperature": 0.7,
  "maxTokens": 512,
  "topP": 0.95,
  "topK": 40,
  "repeatPenalty": 1.1,
  "contextLength": 4096,
  "numThreads": 4,
  "useMmap": true,
  "flashAttention": false,
  "batchSize": 512,
  "gpuLayers": 0
}
```

`gpuLayers` controls how many model layers are offloaded to the GPU. Set to `0` (default) for CPU-only, or `-1` to offload all layers.

#### `POST /v1/generation/mtp/init`

Loads a Multi-Token Prediction (MTP) speculative decoding head from the same GGUF as the generation model. Must be called after `/v1/generation/init`. Supported on models with embedded MTP layers (e.g. Qwen3, GLM-4).

```json
{ "modelPath": "/models/qwen3.gguf", "draftLen": 3 }
```
```json
{ "ok": true }
```

`draftLen` is the maximum number of draft tokens per step (1–8, default `3`). Use `POST /v1/generation/mtp/shutdown` to release MTP resources.

#### Streaming (SSE)

Streaming endpoints (`/stream`, `/streamWithContext`, `/jsonStream`, `/jsonStreamWithContext`, `/continueStream`) respond with `Content-Type: text/event-stream`. Each event is a JSON line:

```
data: {"event":"delta","text":"Kotlin "}
data: {"event":"delta","text":"is great"}
data: {"event":"done"}
```

On error:
```
data: {"event":"error","message":"..."}
```

---

### 💾 KV Session Management (`/v1/generation/session`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/generation/session/reset` | Clear KV state, keep model loaded |
| POST | `/v1/generation/session/save` | Persist KV state to disk |
| POST | `/v1/generation/session/load` | Restore KV state from disk |

#### `POST /v1/generation/session/save`
```json
{ "path": "/data/sessions/chat1.bin" }
```
```json
{ "ok": true }
```

#### `POST /v1/generation/session/load`
```json
{ "path": "/data/sessions/chat1.bin" }
```

After loading, call `POST /v1/generation/continue` to resume generation from the restored state, or `POST /v1/generation/continueStream` for the streaming (SSE) variant.

---

### 🏷 Model Metadata (`/v1/generation/model`)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v1/generation/model/finetuneType` | Returns `general.finetune` GGUF metadata |
| GET | `/v1/generation/model/chatTemplate` | Returns the Jinja chat template embedded in the model |
| POST | `/v1/generation/model/applyChatTemplate` | Renders messages into a prompt using the model's template |

#### `GET /v1/generation/model/finetuneType`
```json
{ "finetuneType": "instruct" }
```

| Value | Meaning |
|---|---|
| `"instruct"` / `"chat"` | Instruction-tuned model |
| `"base"` / `null` | Base model — prompt manually |

#### `POST /v1/generation/model/applyChatTemplate`
```json
{
  "messages": [
    { "role": "system", "content": "You are a helpful assistant." },
    { "role": "user",   "content": "What is Kotlin?" }
  ],
  "addAssistantPrefix": true
}
```
```json
{ "prompt": "<|system|>You are a helpful assistant.<|user|>What is Kotlin?<|assistant|>" }
```

---

### 🎙 Speech-to-Text (`/v1/whisper`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/whisper/init` | Load a Whisper model |
| POST | `/v1/whisper/transcribe` | Transcribe a WAV file (flat text) |
| POST | `/v1/whisper/transcribeSegments` | Transcribe with per-segment timestamps and diarization |
| POST | `/v1/whisper/release` | Free Whisper native resources |

#### `POST /v1/whisper/init`
```json
{ "modelPath": "/models/ggml-tiny-q8_0.bin" }
```

#### `POST /v1/whisper/transcribe`
```json
{
  "wavPath": "/audio/recording.wav",
  "language": "en",
  "initialPrompt": "Technical vocabulary"
}
```
```json
{ "text": "Kotlin Multiplatform is a great technology." }
```

`language` and `initialPrompt` are optional. Provide a 16 kHz, mono, 16-bit PCM WAV for best results.

#### `POST /v1/whisper/transcribeSegments`

Returns a JSON document with per-segment text, start/end timestamps (milliseconds), and speaker-turn markers. Pass `translate: true` to produce an English translation instead of a verbatim transcript. Pass `diarize: true` only with a `…-tdrz` model.

```json
{
  "wavPath": "/audio/recording.wav",
  "language": "en",
  "translate": false,
  "diarize": false
}
```
```json
{
  "json": "{\"language\":\"en\",\"segments\":[{\"text\":\"Hello.\",\"t0\":0,\"t1\":800,\"speaker_turn_next\":false}]}"
}
```

---

### 🎨 Image Generation (`/v1/stablediffusion`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/stablediffusion/init` | Load a Stable Diffusion model |
| POST | `/v1/stablediffusion/txt2img` | Text-to-image generation |
| POST | `/v1/stablediffusion/img2img` | Image-to-image generation |
| POST | `/v1/stablediffusion/release` | Free SD native resources |

Images are exchanged as **Base64-encoded RGBA bytes** (`width × height × 4`).

#### `POST /v1/stablediffusion/init`
```json
{ "modelPath": "/models/dreamshaper.gguf", "threads": 4 }
```

#### `POST /v1/stablediffusion/txt2img`
```json
{
  "prompt": "A cyberpunk llama in neon Tokyo",
  "negativePrompt": "blurry, low quality",
  "width": 512,
  "height": 512,
  "steps": 20,
  "cfgScale": 7.0,
  "seed": 42
}
```
```json
{ "imageBase64": "<base64-encoded RGBA bytes>" }
```

#### `POST /v1/stablediffusion/img2img`
```json
{
  "initImageBase64": "<base64-encoded RGBA source image>",
  "initImageW": 512,
  "initImageH": 512,
  "prompt": "The same scene as a watercolor painting",
  "strength": 0.75,
  "seed": 42
}
```

---

### 👁 Multimodal / VLM (`/v1/multimodal`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/v1/multimodal/init` | Load a VLM model + mmproj |
| POST | `/v1/multimodal/analyzeStream` | Analyze image — streaming SSE |
| POST | `/v1/multimodal/cancel` | Cancel in-progress analysis |
| POST | `/v1/multimodal/release` | Free VLM native resources |

#### `POST /v1/multimodal/init`
```json
{
  "modelPath":   "/models/SmolVLM-256M-Instruct-Q8_0.gguf",
  "mmprojPath":  "/models/mmproj-SmolVLM-256M-f16.gguf"
}
```

#### `POST /v1/multimodal/analyzeStream`

Image input is **Base64-encoded JPEG/PNG/BMP bytes**. The response is an SSE stream using the same format as generation streaming.

```json
{
  "imageBase64": "<base64-encoded image bytes>",
  "prompt": "Describe what you see in this image."
}
```

```
data: {"event":"delta","text":"A llama "}
data: {"event":"delta","text":"standing in a field."}
data: {"event":"done"}
```

---

### 👤 Auth & Profiles

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/v1/users/create` | — | Register a new user, returns JWT |
| POST | `/v1/users/login` | — | Log in, returns JWT |
| POST | `/v1/users/logout` | — | Invalidate session |
| POST | `/v1/profile/create` | JWT | Create user profile |
| GET | `/v1/profile` | JWT | Fetch user profile |
| PATCH | `/v1/profile/update` | JWT | Update user profile |

Pass the JWT as a `Bearer` token in the `Authorization` header for authenticated routes.

---

## 🧪 Testing

The test suite covers all API endpoints using Ktor's `testApplication` with service fakes — no native runtime or model files required.

```bash
./gradlew test
```

| Suite | Tests |
|---|---|
| `EmbeddingRoutesTest` | 4 |
| `GenerationRoutesTest` | 31 |
| `WhisperRoutesTest` | 10 |
| `StableDiffusionRoutesTest` | 10 |
| `MultimodalRoutesTest` | 7 |
| `SseTest` | 3 |
| **Total** | **65** |

---

## ⚙️ CI

A GitHub Actions workflow (`.github/workflows/ci.yml`) runs `./gradlew test` on every pull request to `main` and on every push. Concurrent runs for the same ref are cancelled automatically.

To block merging on test failure, enable **Settings → Branches → Require status checks → Build & Test** in your repository.

---

## 🔄 Hybrid Mode (Local + Remote)

Llamatik is designed for **offline-first apps**.

You can:
- Run inference locally via llama.cpp / whisper.cpp / stable-diffusion.cpp
- Fall back to this server when more power is needed
- Switch dynamically based on connectivity or hardware capability

---

## 🌍 Production Deployment

For production usage:

- Add HTTPS via a reverse proxy (Nginx / Caddy)
- Use container orchestration (Docker Compose / Kubernetes)
- Configure resource limits
- Enable JWT authentication for all inference routes

Example:

```
Internet
   │
Reverse Proxy (TLS)
   │
Llamatik Server (Docker)
   │
llama.cpp / whisper.cpp / stable-diffusion.cpp runtimes
```

---

## 📦 Related Projects

- 🔗 **Llamatik Library** — Kotlin Multiplatform AI SDK
  https://github.com/ferranpons/llamatik

---

## 🤝 Contributing

Contributions are welcome:
- New endpoint coverage
- Performance improvements
- Deployment enhancements
- Documentation updates

Open an issue or PR 🚀

---

## 📜 License

This project is licensed under the MIT License.
See [LICENSE](./LICENSE) for details.

---

Built with ❤️ for the Kotlin & AI community.
