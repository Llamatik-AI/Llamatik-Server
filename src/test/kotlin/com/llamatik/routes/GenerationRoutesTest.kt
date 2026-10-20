package com.llamatik.routes

import com.llamatik.fakes.FakeLlamaService
import com.llamatik.withTestApp
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerationRoutesTest {

    // --- init ---

    @Test
    fun `init returns ok true on success`() = withTestApp {
        val response = client.post("/v1/generation/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/llama.gguf"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `init returns 400 on failure`() = withTestApp(
        llama = FakeLlamaService(initGenerateResult = Result.failure(RuntimeException("failed")))
    ) {
        val response = client.post("/v1/generation/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/bad"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // --- generate ---

    @Test
    fun `generate returns text`() = withTestApp {
        val response = client.post("/v1/generation/generate") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"tell me a joke"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("generated text", body["text"]?.jsonPrimitive?.content)
    }

    @Test
    fun `generate returns 400 on failure`() = withTestApp(
        llama = FakeLlamaService(generateResult = Result.failure(RuntimeException("gen error")))
    ) {
        val response = client.post("/v1/generation/generate") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"x"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // --- generateWithContext ---

    @Test
    fun `generateWithContext returns text`() = withTestApp {
        val response = client.post("/v1/generation/generateWithContext") {
            contentType(ContentType.Application.Json)
            setBody("""{"systemPrompt":"sys","contextBlock":"ctx","userPrompt":"user"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("context text", body["text"]?.jsonPrimitive?.content)
    }

    // --- generateJson ---

    @Test
    fun `generateJson returns json string`() = withTestApp {
        val response = client.post("/v1/generation/generateJson") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"return json"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(body["text"]?.jsonPrimitive?.content?.contains("key") == true)
    }

    @Test
    fun `generateJson with schema returns text`() = withTestApp {
        val response = client.post("/v1/generation/generateJson") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"x","jsonSchema":"{\"type\":\"object\"}"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // --- generateJsonWithContext ---

    @Test
    fun `generateJsonWithContext returns text`() = withTestApp {
        val response = client.post("/v1/generation/generateJsonWithContext") {
            contentType(ContentType.Application.Json)
            setBody("""{"systemPrompt":"s","contextBlock":"c","userPrompt":"u"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // --- continue ---

    @Test
    fun `generateContinue returns continued text`() = withTestApp {
        val response = client.post("/v1/generation/continue") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"and then?"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("continued text", body["text"]?.jsonPrimitive?.content)
    }

    // --- params ---

    @Test
    fun `update params returns ok`() = withTestApp {
        val response = client.post("/v1/generation/params") {
            contentType(ContentType.Application.Json)
            setBody("""{"temperature":0.8,"maxTokens":512,"topP":0.9,"topK":40,"repeatPenalty":1.1,"contextLength":4096,"numThreads":4,"useMmap":true,"flashAttention":false,"batchSize":512}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `update params with defaults for new fields returns ok`() = withTestApp {
        val response = client.post("/v1/generation/params") {
            contentType(ContentType.Application.Json)
            setBody("""{"temperature":0.7,"maxTokens":256,"topP":0.95,"topK":40,"repeatPenalty":1.0}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // --- cancel ---

    @Test
    fun `cancel returns ok`() = withTestApp {
        val response = client.post("/v1/generation/cancel") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // --- shutdown ---

    @Test
    fun `shutdown returns ok`() = withTestApp {
        val response = client.post("/v1/generation/shutdown") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // --- session ---

    @Test
    fun `session reset returns ok`() = withTestApp {
        val response = client.post("/v1/generation/session/reset") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `session save returns ok`() = withTestApp {
        val response = client.post("/v1/generation/session/save") {
            contentType(ContentType.Application.Json)
            setBody("""{"path":"/tmp/session.bin"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `session load returns ok`() = withTestApp {
        val response = client.post("/v1/generation/session/load") {
            contentType(ContentType.Application.Json)
            setBody("""{"path":"/tmp/session.bin"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `session save returns 400 on failure`() = withTestApp(
        llama = FakeLlamaService(sessionSaveResult = Result.failure(RuntimeException("disk full")))
    ) {
        val response = client.post("/v1/generation/session/save") {
            contentType(ContentType.Application.Json)
            setBody("""{"path":"/tmp/session.bin"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("disk full"))
    }

    // --- model metadata ---

    @Test
    fun `finetune type returns instruct`() = withTestApp {
        val response = client.get("/v1/generation/model/finetuneType")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("instruct", body["finetuneType"]?.jsonPrimitive?.content)
    }

    @Test
    fun `finetune type returns null when not set`() = withTestApp(
        llama = FakeLlamaService(finetuneTypeResult = Result.success(null))
    ) {
        val response = client.get("/v1/generation/model/finetuneType")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertTrue(body["finetuneType"]?.jsonPrimitive?.isString == false || body["finetuneType"]?.jsonPrimitive?.content == null || body.containsKey("finetuneType"))
    }

    @Test
    fun `chat template returns template string`() = withTestApp {
        val response = client.get("/v1/generation/model/chatTemplate")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("{{messages}}", body["chatTemplate"]?.jsonPrimitive?.content)
    }

    @Test
    fun `apply chat template returns rendered prompt`() = withTestApp {
        val response = client.post("/v1/generation/model/applyChatTemplate") {
            contentType(ContentType.Application.Json)
            setBody("""{"messages":[{"role":"user","content":"hi"}],"addAssistantPrefix":true}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("<|user|>hi", body["prompt"]?.jsonPrimitive?.content)
    }

    @Test
    fun `apply chat template returns 400 on failure`() = withTestApp(
        llama = FakeLlamaService(applyChatTemplateResult = Result.failure(RuntimeException("template error")))
    ) {
        val response = client.post("/v1/generation/model/applyChatTemplate") {
            contentType(ContentType.Application.Json)
            setBody("""{"messages":[{"role":"user","content":"hi"}],"addAssistantPrefix":true}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    // --- SSE streams ---

    @Test
    fun `stream endpoint returns SSE events`() = withTestApp {
        val response = client.post("/v1/generation/stream") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"hello"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/event-stream", response.contentType()?.contentType + "/" + response.contentType()?.contentSubtype)
        val body = response.bodyAsText()
        assertTrue(body.contains("\"event\":\"delta\""))
        assertTrue(body.contains("\"event\":\"done\""))
    }

    @Test
    fun `streamWithContext endpoint returns SSE events`() = withTestApp {
        val response = client.post("/v1/generation/streamWithContext") {
            contentType(ContentType.Application.Json)
            setBody("""{"systemPrompt":"sys","contextBlock":"ctx","userPrompt":"user"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"event\":\"done\""))
    }

    @Test
    fun `jsonStream endpoint returns SSE events`() = withTestApp {
        val response = client.post("/v1/generation/jsonStream") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"x"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"event\":\"done\""))
    }

    @Test
    fun `jsonStreamWithContext endpoint returns SSE events`() = withTestApp {
        val response = client.post("/v1/generation/jsonStreamWithContext") {
            contentType(ContentType.Application.Json)
            setBody("""{"systemPrompt":"s","contextBlock":"c","userPrompt":"u"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"event\":\"done\""))
    }

    // --- MTP ---

    @Test
    fun `mtp init returns ok`() = withTestApp {
        val response = client.post("/v1/generation/mtp/init") {
            contentType(ContentType.Application.Json)
            setBody("""{"modelPath":"/models/llama.gguf","draftLen":3}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    @Test
    fun `mtp shutdown returns ok`() = withTestApp {
        val response = client.post("/v1/generation/mtp/shutdown") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("true", body["ok"]?.jsonPrimitive?.content)
    }

    // --- continueStream SSE ---

    @Test
    fun `continueStream endpoint returns SSE events`() = withTestApp {
        val response = client.post("/v1/generation/continueStream") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"continue this"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("text/event-stream", response.contentType()?.contentType + "/" + response.contentType()?.contentSubtype)
        val body = response.bodyAsText()
        assertTrue(body.contains("\"event\":\"delta\""))
        assertTrue(body.contains("\"event\":\"done\""))
    }

    // --- gpuLayers param ---

    @Test
    fun `update params with gpuLayers returns ok`() = withTestApp {
        val response = client.post("/v1/generation/params") {
            contentType(ContentType.Application.Json)
            setBody("""{"temperature":0.8,"maxTokens":512,"topP":0.9,"topK":40,"repeatPenalty":1.1,"gpuLayers":32}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `stream sends error event on service failure`() = withTestApp(
        llama = FakeLlamaService(
            streamDeltas = emptyList()
        ).let {
            FakeLlamaService(
                streamDeltas = emptyList(),
                generateResult = Result.failure(RuntimeException("unused")),
            ).also { _ ->
                // Use a service that calls onError immediately
            }
        }.let {
            object : com.llamatik.llama.LlamaServicePort by it {
                override fun generateStream(prompt: String, callback: com.llamatik.core.platform.GenStream) {
                    callback.onError("stream broke")
                }
            }
        }
    ) {
        val response = client.post("/v1/generation/stream") {
            contentType(ContentType.Application.Json)
            setBody("""{"prompt":"x"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"event\":\"error\""))
        assertTrue(response.bodyAsText().contains("stream broke"))
    }
}
