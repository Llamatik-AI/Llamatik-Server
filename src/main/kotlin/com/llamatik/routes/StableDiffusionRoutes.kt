package com.llamatik.routes

import com.llamatik.API_VERSION
import com.llamatik.api.ImageResponse
import com.llamatik.api.Img2ImgRequest
import com.llamatik.api.InitModelResponse
import com.llamatik.api.OkResponse
import com.llamatik.api.StableDiffusionInitRequest
import com.llamatik.api.Txt2ImgRequest
import com.llamatik.stablediffusion.StableDiffusionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import java.util.Base64

private const val SD = "$API_VERSION/stablediffusion"
private const val SD_INIT = "$SD/init"
private const val SD_TXT2IMG = "$SD/txt2img"
private const val SD_IMG2IMG = "$SD/img2img"
private const val SD_RELEASE = "$SD/release"

fun Route.stableDiffusionRoutes() {
    post(SD_INIT) {
        val req = call.receive<StableDiffusionInitRequest>()
        val res = StableDiffusionService.initModel(req.modelPath, req.threads)
        res.fold(
            onSuccess = { call.respond(InitModelResponse(ok = it)) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "SD init failed") }
        )
    }

    post(SD_TXT2IMG) {
        val req = call.receive<Txt2ImgRequest>()
        val res = StableDiffusionService.txt2img(
            prompt = req.prompt,
            negativePrompt = req.negativePrompt,
            width = req.width,
            height = req.height,
            steps = req.steps,
            cfgScale = req.cfgScale,
            seed = req.seed,
        )
        res.fold(
            onSuccess = { bytes ->
                if (bytes.isEmpty()) {
                    call.respond(HttpStatusCode.InternalServerError, "Image generation produced no output")
                } else {
                    call.respond(ImageResponse(imageBase64 = Base64.getEncoder().encodeToString(bytes)))
                }
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "txt2img failed") }
        )
    }

    post(SD_IMG2IMG) {
        val req = call.receive<Img2ImgRequest>()
        val initBytes = Base64.getDecoder().decode(req.initImageBase64)
        val res = StableDiffusionService.img2img(
            initImageRgba = initBytes,
            initImageW = req.initImageW,
            initImageH = req.initImageH,
            prompt = req.prompt,
            negativePrompt = req.negativePrompt,
            width = req.width,
            height = req.height,
            steps = req.steps,
            cfgScale = req.cfgScale,
            strength = req.strength,
            seed = req.seed,
        )
        res.fold(
            onSuccess = { bytes ->
                if (bytes.isEmpty()) {
                    call.respond(HttpStatusCode.InternalServerError, "Image generation produced no output")
                } else {
                    call.respond(ImageResponse(imageBase64 = Base64.getEncoder().encodeToString(bytes)))
                }
            },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "img2img failed") }
        )
    }

    post(SD_RELEASE) {
        val res = StableDiffusionService.release()
        res.fold(
            onSuccess = { call.respond(OkResponse()) },
            onFailure = { call.respond(HttpStatusCode.BadRequest, it.message ?: "SD release failed") }
        )
    }
}
