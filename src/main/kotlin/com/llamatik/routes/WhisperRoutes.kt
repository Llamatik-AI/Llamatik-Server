package com.llamatik.routes

import com.llamatik.API_VERSION
import com.llamatik.api.InitModelResponse
import com.llamatik.api.OkResponse
import com.llamatik.api.TranscribeRequest
import com.llamatik.api.TranscribeResponse
import com.llamatik.api.WhisperInitRequest
import com.llamatik.whisper.WhisperService
import com.llamatik.whisper.WhisperServicePort
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

private const val WHISPER = "$API_VERSION/whisper"
private const val WHISPER_INIT = "$WHISPER/init"
private const val WHISPER_TRANSCRIBE = "$WHISPER/transcribe"
private const val WHISPER_RELEASE = "$WHISPER/release"

fun Route.whisperRoutes(service: WhisperServicePort = WhisperService) {
    post(WHISPER_INIT) {
        val req = call.receive<WhisperInitRequest>()
        service.initModel(req.modelPath).fold(
            onSuccess = { call.respond(InitModelResponse(ok = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Whisper init failed") }
        )
    }

    post(WHISPER_TRANSCRIBE) {
        val req = call.receive<TranscribeRequest>()
        service.transcribeWav(req.wavPath, req.language, req.initialPrompt).fold(
            onSuccess = { call.respond(TranscribeResponse(text = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Transcription failed") }
        )
    }

    post(WHISPER_RELEASE) {
        service.release().fold(
            onSuccess = { call.respond(OkResponse()) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "Whisper release failed") }
        )
    }
}
