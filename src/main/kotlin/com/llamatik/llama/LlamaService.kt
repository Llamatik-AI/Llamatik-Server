package com.llamatik.llama

import com.llamatik.library.platform.GenStream
import com.llamatik.library.platform.LlamaBridge
import com.llamatik.llama.LlamaService.mutex
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * JVM-side façade over the KMP [LlamaBridge] for use in the Ktor backend.
 *
 * The llama.cpp JNI integration is typically not re-entrant. This service
 * serializes calls that mutate or use the current generation session.
 */
object LlamaService {

    private val mutex = Mutex()

    /** Initialize an embedding-capable model. */
    suspend fun initModel(modelPath: String): Result<Boolean> = runCatching {
        mutex.withLock { LlamaBridge.initEmbedModel(modelPath) }
    }

    /** Compute embeddings for a single input. */
    suspend fun embed(input: String): Result<FloatArray> = runCatching {
        mutex.withLock { LlamaBridge.embed(input) }
    }

    /** Initialize a generation-capable model. */
    suspend fun initGenerateModel(modelPath: String): Result<Boolean> = runCatching {
        mutex.withLock { LlamaBridge.initGenerateModel(modelPath) }
    }

    suspend fun generate(prompt: String): Result<String> = runCatching {
        mutex.withLock { LlamaBridge.generate(prompt) }
    }

    suspend fun generateWithContext(systemPrompt: String, contextBlock: String, userPrompt: String): Result<String> =
        runCatching {
            mutex.withLock { LlamaBridge.generateWithContext(systemPrompt, contextBlock, userPrompt) }
        }

    suspend fun generateJson(prompt: String, jsonSchema: String?): Result<String> = runCatching {
        mutex.withLock { LlamaBridge.generateJson(prompt, jsonSchema) }
    }

    suspend fun generateJsonWithContext(
        systemPrompt: String,
        contextBlock: String,
        userPrompt: String,
        jsonSchema: String?
    ): Result<String> = runCatching {
        mutex.withLock { LlamaBridge.generateJsonWithContext(systemPrompt, contextBlock, userPrompt, jsonSchema) }
    }

    suspend fun updateGenerateParams(
        temperature: Float,
        maxTokens: Int,
        topP: Float,
        topK: Int,
        repeatPenalty: Float,
        contextLength: Int,
        numThreads: Int,
        useMmap: Boolean,
        flashAttention: Boolean,
        batchSize: Int,
    ): Result<Unit> = runCatching {
        mutex.withLock {
            LlamaBridge.updateGenerateParams(
                temperature = temperature,
                maxTokens = maxTokens,
                topP = topP,
                topK = topK,
                repeatPenalty = repeatPenalty,
                contextLength = contextLength,
                numThreads = numThreads,
                useMmap = useMmap,
                flashAttention = flashAttention,
                batchSize = batchSize,
            )
        }
    }

    suspend fun generateContinue(prompt: String): Result<String> = runCatching {
        mutex.withLock { LlamaBridge.generateContinue(prompt) }
    }

    suspend fun sessionReset(): Result<Boolean> = runCatching {
        mutex.withLock { LlamaBridge.sessionReset() }
    }

    suspend fun sessionSave(path: String): Result<Boolean> = runCatching {
        mutex.withLock { LlamaBridge.sessionSave(path) }
    }

    suspend fun sessionLoad(path: String): Result<Boolean> = runCatching {
        mutex.withLock { LlamaBridge.sessionLoad(path) }
    }

    suspend fun getModelFinetuneType(): Result<String?> = runCatching {
        mutex.withLock { LlamaBridge.getModelFinetuneType() }
    }

    suspend fun getModelChatTemplate(): Result<String?> = runCatching {
        mutex.withLock { LlamaBridge.getModelChatTemplate() }
    }

    suspend fun applyChatTemplate(messages: List<Pair<String, String>>, addAssistantPrefix: Boolean): Result<String?> =
        runCatching {
            mutex.withLock { LlamaBridge.applyChatTemplate(messages, addAssistantPrefix) }
        }

    /**
     * Streaming generation using the native callback.
     *
     * This call is not protected by [mutex] because the native layer usually blocks until completion anyway.
     * If your JNI implementation is re-entrant, you may remove other locks. If it is *not*, consider
     * surrounding this call with [mutex] too.
     */
    fun generateStream(prompt: String, callback: GenStream) {
        LlamaBridge.generateStream(prompt, callback)
    }

    fun generateStreamWithContext(systemPrompt: String, contextBlock: String, userPrompt: String, callback: GenStream) {
        LlamaBridge.generateStreamWithContext(systemPrompt, contextBlock, userPrompt, callback)
    }

    fun generateJsonStream(prompt: String, jsonSchema: String?, callback: GenStream) {
        LlamaBridge.generateJsonStream(prompt, jsonSchema, callback)
    }

    fun generateJsonStreamWithContext(
        systemPrompt: String,
        contextBlock: String,
        userPrompt: String,
        jsonSchema: String?,
        callback: GenStream
    ) {
        LlamaBridge.generateJsonStreamWithContext(systemPrompt, contextBlock, userPrompt, jsonSchema, callback)
    }

    suspend fun cancelGenerate(): Result<Unit> = runCatching {
        mutex.withLock { LlamaBridge.nativeCancelGenerate() }
    }

    suspend fun shutdown(): Result<Unit> = runCatching {
        mutex.withLock { LlamaBridge.shutdown() }
    }
}
