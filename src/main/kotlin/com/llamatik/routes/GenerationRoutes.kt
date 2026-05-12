package com.llamatik.routes

import com.llamatik.API_VERSION
import com.llamatik.api.ApplyChatTemplateRequest
import com.llamatik.api.ApplyChatTemplateResponse
import com.llamatik.api.GenerateContinueRequest
import com.llamatik.api.GenerateJsonRequest
import com.llamatik.api.GenerateJsonWithContextRequest
import com.llamatik.api.GenerateRequest
import com.llamatik.api.GenerateResponse
import com.llamatik.api.GenerateWithContextRequest
import com.llamatik.api.InitModelRequest
import com.llamatik.api.InitModelResponse
import com.llamatik.api.ModelChatTemplateResponse
import com.llamatik.api.ModelFinetuneTypeResponse
import com.llamatik.api.OkResponse
import com.llamatik.api.SessionLoadRequest
import com.llamatik.api.SessionResult
import com.llamatik.api.SessionSaveRequest
import com.llamatik.api.UpdateParamsRequest
import com.llamatik.library.platform.GenStream
import com.llamatik.llama.LlamaService
import com.llamatik.util.Sse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch

private const val GENERATION = "$API_VERSION/generation"
private const val STREAM_DELTA_BUFFER_CAPACITY = 64

private const val GENERATION_INIT = "$GENERATION/init"
private const val GENERATION_GENERATE = "$GENERATION/generate"
private const val GENERATION_GENERATE_WITH_CONTEXT = "$GENERATION/generateWithContext"
private const val GENERATION_GENERATE_JSON = "$GENERATION/generateJson"
private const val GENERATION_GENERATE_JSON_WITH_CONTEXT = "$GENERATION/generateJsonWithContext"
private const val GENERATION_STREAM = "$GENERATION/stream"
private const val GENERATION_STREAM_WITH_CONTEXT = "$GENERATION/streamWithContext"
private const val GENERATION_JSON_STREAM = "$GENERATION/jsonStream"
private const val GENERATION_JSON_STREAM_WITH_CONTEXT = "$GENERATION/jsonStreamWithContext"
private const val GENERATION_PARAMS = "$GENERATION/params"
private const val GENERATION_CANCEL = "$GENERATION/cancel"
private const val GENERATION_SHUTDOWN = "$GENERATION/shutdown"
private const val GENERATION_CONTINUE = "$GENERATION/continue"
private const val GENERATION_SESSION_RESET = "$GENERATION/session/reset"
private const val GENERATION_SESSION_SAVE = "$GENERATION/session/save"
private const val GENERATION_SESSION_LOAD = "$GENERATION/session/load"
private const val GENERATION_MODEL_FINETUNE_TYPE = "$GENERATION/model/finetuneType"
private const val GENERATION_MODEL_CHAT_TEMPLATE = "$GENERATION/model/chatTemplate"
private const val GENERATION_MODEL_APPLY_CHAT_TEMPLATE = "$GENERATION/model/applyChatTemplate"

/**
 * Mirrors the Llamatik library API for generation.
 */
@Suppress("TooGenericExceptionCaught")
fun Route.generationRoutes() {

    // --- initialization ---
    post(GENERATION_INIT) {
        val req = call.receive<InitModelRequest>()
        val res = LlamaService.initGenerateModel(req.modelPath)
        res.fold(
            onSuccess = { call.respond(InitModelResponse(ok = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Init failed") }
        )
    }

    // --- non-streaming ---
    post(GENERATION_GENERATE) {
        val req = call.receive<GenerateRequest>()
        val res = LlamaService.generate(req.prompt)
        res.fold(
            onSuccess = { call.respond(GenerateResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Generation failed") }
        )
    }

    post(GENERATION_GENERATE_WITH_CONTEXT) {
        val req = call.receive<GenerateWithContextRequest>()
        val res = LlamaService.generateWithContext(req.systemPrompt, req.contextBlock, req.userPrompt)
        res.fold(
            onSuccess = { call.respond(GenerateResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Generation failed") }
        )
    }

    post(GENERATION_GENERATE_JSON) {
        val req = call.receive<GenerateJsonRequest>()
        val res = LlamaService.generateJson(req.prompt, req.jsonSchema)
        res.fold(
            onSuccess = { call.respond(GenerateResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Generation failed") }
        )
    }

    post(GENERATION_GENERATE_JSON_WITH_CONTEXT) {
        val req = call.receive<GenerateJsonWithContextRequest>()
        val res = LlamaService.generateJsonWithContext(req.systemPrompt, req.contextBlock, req.userPrompt, req.jsonSchema)
        res.fold(
            onSuccess = { call.respond(GenerateResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Generation failed") }
        )
    }

    post(GENERATION_CONTINUE) {
        val req = call.receive<GenerateContinueRequest>()
        val res = LlamaService.generateContinue(req.prompt)
        res.fold(
            onSuccess = { call.respond(GenerateResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Generation failed") }
        )
    }

    // --- params / cancel / shutdown ---
    post(GENERATION_PARAMS) {
        val req = call.receive<UpdateParamsRequest>()
        val res = LlamaService.updateGenerateParams(
            req.temperature, req.maxTokens, req.topP, req.topK, req.repeatPenalty,
            req.contextLength, req.numThreads, req.useMmap, req.flashAttention, req.batchSize
        )
        res.fold(
            onSuccess = { call.respond(OkResponse()) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Param update failed") }
        )
    }

    post(GENERATION_CANCEL) {
        val res = LlamaService.cancelGenerate()
        res.fold(
            onSuccess = { call.respond(OkResponse()) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Cancel failed") }
        )
    }

    post(GENERATION_SHUTDOWN) {
        val res = LlamaService.shutdown()
        res.fold(
            onSuccess = { call.respond(OkResponse()) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Shutdown failed") }
        )
    }

    // --- KV session management ---
    post(GENERATION_SESSION_RESET) {
        val res = LlamaService.sessionReset()
        res.fold(
            onSuccess = { call.respond(SessionResult(ok = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Session reset failed") }
        )
    }

    post(GENERATION_SESSION_SAVE) {
        val req = call.receive<SessionSaveRequest>()
        val res = LlamaService.sessionSave(req.path)
        res.fold(
            onSuccess = { call.respond(SessionResult(ok = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Session save failed") }
        )
    }

    post(GENERATION_SESSION_LOAD) {
        val req = call.receive<SessionLoadRequest>()
        val res = LlamaService.sessionLoad(req.path)
        res.fold(
            onSuccess = { call.respond(SessionResult(ok = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Session load failed") }
        )
    }

    // --- model metadata ---
    get(GENERATION_MODEL_FINETUNE_TYPE) {
        val res = LlamaService.getModelFinetuneType()
        res.fold(
            onSuccess = { call.respond(ModelFinetuneTypeResponse(finetuneType = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Failed to get finetune type") }
        )
    }

    get(GENERATION_MODEL_CHAT_TEMPLATE) {
        val res = LlamaService.getModelChatTemplate()
        res.fold(
            onSuccess = { call.respond(ModelChatTemplateResponse(chatTemplate = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Failed to get chat template") }
        )
    }

    post(GENERATION_MODEL_APPLY_CHAT_TEMPLATE) {
        val req = call.receive<ApplyChatTemplateRequest>()
        val pairs = req.messages.map { it.role to it.content }
        val res = LlamaService.applyChatTemplate(pairs, req.addAssistantPrefix)
        res.fold(
            onSuccess = { call.respond(ApplyChatTemplateResponse(prompt = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Failed to apply chat template") }
        )
    }

    // --- streaming (SSE) ---
    postSseStream<GenerateRequest>(GENERATION_STREAM) { req, cb ->
        LlamaService.generateStream(req.prompt, cb)
    }

    postSseStream<GenerateWithContextRequest>(GENERATION_STREAM_WITH_CONTEXT) { req, cb ->
        LlamaService.generateStreamWithContext(req.systemPrompt, req.contextBlock, req.userPrompt, cb)
    }

    postSseStream<GenerateJsonRequest>(GENERATION_JSON_STREAM) { req, cb ->
        LlamaService.generateJsonStream(req.prompt, req.jsonSchema, cb)
    }

    postSseStream<GenerateJsonWithContextRequest>(GENERATION_JSON_STREAM_WITH_CONTEXT) { req, cb ->
        LlamaService.generateJsonStreamWithContext(
            req.systemPrompt,
            req.contextBlock,
            req.userPrompt,
            req.jsonSchema,
            cb
        )
    }
}

@Suppress("TooGenericExceptionCaught")
private inline fun <reified T : Any> Route.postSseStream(
    path: String,
    crossinline stream: (T, GenStream) -> Unit
) {
    post(path) {
        val req = call.receive<T>()

        call.respondTextWriter(contentType = ContentType.Text.EventStream) {
            val deltas = Channel<String>(capacity = STREAM_DELTA_BUFFER_CAPACITY)
            val done = Channel<Unit>(capacity = 1)
            val errors = Channel<String>(capacity = 1)
            val cb = Sse.genStreamCallback(
                // A bounded buffer applies backpressure so slow clients cannot grow server memory without limit.
                onDelta = { deltas.trySendBlocking(it).isSuccess },
                onDone = { done.trySend(Unit).isSuccess },
                onError = { errors.trySend(it).isSuccess }
            )

            val streamJob = CoroutineScope(currentCoroutineContext()).launch(Dispatchers.IO) {
                try {
                    stream(req, cb)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    errors.trySend(e.message ?: "Streaming failed")
                }
            }

            try {
                Sse.pipe(writer = this, deltas = deltas, done = done, errors = errors)
            } finally {
                streamJob.cancel()
            }
        }
    }
}
