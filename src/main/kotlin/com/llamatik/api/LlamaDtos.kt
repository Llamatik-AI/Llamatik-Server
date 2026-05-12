package com.llamatik.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InitModelRequest(
    val modelPath: String
)

@Serializable
data class InitModelResponse(
    val ok: Boolean
)

@Serializable
data class EmbedRequest(
    val input: String
)

@Serializable
data class EmbedResponse(
    val embedding: List<Float>
)

@Serializable
data class GenerateRequest(
    val prompt: String
)

@Serializable
data class GenerateWithContextRequest(
    val systemPrompt: String,
    val contextBlock: String,
    val userPrompt: String
)

@Serializable
data class GenerateJsonRequest(
    val prompt: String,
    val jsonSchema: String? = null
)

@Serializable
data class GenerateJsonWithContextRequest(
    val systemPrompt: String,
    val contextBlock: String,
    val userPrompt: String,
    val jsonSchema: String? = null
)

@Serializable
data class GenerateResponse(
    val text: String
)

@Serializable
data class UpdateParamsRequest(
    val temperature: Float,
    val maxTokens: Int,
    @SerialName("topP") val topP: Float,
    @SerialName("topK") val topK: Int,
    val repeatPenalty: Float,
    val contextLength: Int = 4096,
    val numThreads: Int = 4,
    val useMmap: Boolean = true,
    val flashAttention: Boolean = false,
    val batchSize: Int = 512,
)

@Serializable
data class OkResponse(
    val ok: Boolean = true
)

// --- session ---

@Serializable
data class SessionSaveRequest(val path: String)

@Serializable
data class SessionLoadRequest(val path: String)

@Serializable
data class SessionResult(val ok: Boolean)

// --- generateContinue ---

@Serializable
data class GenerateContinueRequest(val prompt: String)

// --- model metadata ---

@Serializable
data class ModelFinetuneTypeResponse(val finetuneType: String?)

@Serializable
data class ModelChatTemplateResponse(val chatTemplate: String?)

@Serializable
data class ChatMessage(val role: String, val content: String)

@Serializable
data class ApplyChatTemplateRequest(
    val messages: List<ChatMessage>,
    val addAssistantPrefix: Boolean = true
)

@Serializable
data class ApplyChatTemplateResponse(val prompt: String?)

// --- Whisper (STT) ---

@Serializable
data class WhisperInitRequest(val modelPath: String)

@Serializable
data class TranscribeRequest(
    val wavPath: String,
    val language: String? = null,
    val initialPrompt: String? = null
)

@Serializable
data class TranscribeResponse(val text: String)

// --- Stable Diffusion ---

@Serializable
data class StableDiffusionInitRequest(val modelPath: String, val threads: Int = -1)

@Serializable
data class Txt2ImgRequest(
    val prompt: String,
    val negativePrompt: String? = null,
    val width: Int = 512,
    val height: Int = 512,
    val steps: Int = 20,
    val cfgScale: Float = 7.0f,
    val seed: Long = -1L,
)

@Serializable
data class Img2ImgRequest(
    val initImageBase64: String,
    val initImageW: Int,
    val initImageH: Int,
    val prompt: String,
    val negativePrompt: String? = null,
    val width: Int = 512,
    val height: Int = 512,
    val steps: Int = 20,
    val cfgScale: Float = 7.0f,
    val strength: Float = 0.75f,
    val seed: Long = -1L,
)

@Serializable
data class ImageResponse(val imageBase64: String)

// --- Multimodal (VLM) ---

@Serializable
data class MultimodalInitRequest(val modelPath: String, val mmprojPath: String)

@Serializable
data class AnalyzeImageRequest(
    val imageBase64: String,
    val prompt: String
)
