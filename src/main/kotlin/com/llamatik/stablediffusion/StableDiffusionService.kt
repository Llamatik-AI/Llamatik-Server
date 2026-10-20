package com.llamatik.stablediffusion

import com.llamatik.core.platform.StableDiffusionBridge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object StableDiffusionService : StableDiffusionServicePort {

    private val mutex = Mutex()

    override suspend fun initModel(modelPath: String, threads: Int): Result<Boolean> = runCatching {
        mutex.withLock { StableDiffusionBridge.initModel(modelPath, threads) }
    }

    override suspend fun txt2img(
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        seed: Long,
    ): Result<ByteArray> = runCatching {
        mutex.withLock {
            StableDiffusionBridge.txt2img(
                prompt = prompt,
                negativePrompt = negativePrompt,
                width = width,
                height = height,
                steps = steps,
                cfgScale = cfgScale,
                seed = seed,
            )
        }
    }

    override suspend fun img2img(
        initImageRgba: ByteArray,
        initImageW: Int,
        initImageH: Int,
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfgScale: Float,
        strength: Float,
        seed: Long,
    ): Result<ByteArray> = runCatching {
        mutex.withLock {
            StableDiffusionBridge.img2img(
                initImageRgba = initImageRgba,
                initImageW = initImageW,
                initImageH = initImageH,
                prompt = prompt,
                negativePrompt = negativePrompt,
                width = width,
                height = height,
                steps = steps,
                cfgScale = cfgScale,
                strength = strength,
                seed = seed,
            )
        }
    }

    override suspend fun release(): Result<Unit> = runCatching {
        mutex.withLock { StableDiffusionBridge.release() }
    }
}
