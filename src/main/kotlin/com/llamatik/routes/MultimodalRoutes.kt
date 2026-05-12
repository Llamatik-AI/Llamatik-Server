package com.llamatik.routes

import com.llamatik.API_VERSION
import com.llamatik.api.AnalyzeImageRequest
import com.llamatik.api.InitModelResponse
import com.llamatik.api.MultimodalInitRequest
import com.llamatik.api.OkResponse
import com.llamatik.library.platform.GenStream
import com.llamatik.multimodal.MultimodalService
import com.llamatik.multimodal.MultimodalServicePort
import com.llamatik.util.Sse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import java.util.Base64

private const val VLM = "$API_VERSION/multimodal"
private const val VLM_INIT = "$VLM/init"
private const val VLM_ANALYZE_STREAM = "$VLM/analyzeStream"
private const val VLM_CANCEL = "$VLM/cancel"
private const val VLM_RELEASE = "$VLM/release"

@Suppress("TooGenericExceptionCaught")
fun Route.multimodalRoutes(service: MultimodalServicePort = MultimodalService) {
    post(VLM_INIT) {
        val req = call.receive<MultimodalInitRequest>()
        service.initModel(req.modelPath, req.mmprojPath).fold(
            onSuccess = { call.respond(InitModelResponse(ok = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "VLM init failed") }
        )
    }

    post(VLM_ANALYZE_STREAM) {
        val req = call.receive<AnalyzeImageRequest>()
        val imageBytes = Base64.getDecoder().decode(req.imageBase64)

        call.respondTextWriter(contentType = ContentType.Text.EventStream) {
            val deltas = Channel<String>(capacity = 64)
            val done = Channel<Unit>(capacity = 1)
            val errors = Channel<String>(capacity = 1)
            val cb: GenStream = Sse.genStreamCallback(
                onDelta = { deltas.trySendBlocking(it).isSuccess },
                onDone = { done.trySend(Unit).isSuccess },
                onError = { errors.trySend(it).isSuccess }
            )

            val streamJob = CoroutineScope(currentCoroutineContext()).launch(Dispatchers.IO) {
                try {
                    service.analyzeImageBytesStream(imageBytes, req.prompt, cb)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    errors.trySend(e.message ?: "VLM streaming failed")
                }
            }

            try {
                Sse.pipe(writer = this, deltas = deltas, done = done, errors = errors)
            } finally {
                streamJob.cancel()
            }
        }
    }

    post(VLM_CANCEL) {
        service.cancelAnalysis().fold(
            onSuccess = { call.respond(OkResponse()) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "VLM cancel failed") }
        )
    }

    post(VLM_RELEASE) {
        service.release().fold(
            onSuccess = { call.respond(OkResponse()) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "VLM release failed") }
        )
    }
}
