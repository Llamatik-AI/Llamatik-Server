package com.llamatik.fakes

import com.llamatik.stablediffusion.StableDiffusionServicePort

class FakeStableDiffusionService(
    private val initResult: Result<Boolean> = Result.success(true),
    private val txt2imgResult: Result<ByteArray> = Result.success(ByteArray(512 * 512 * 4) { 0xFF.toByte() }),
    private val img2imgResult: Result<ByteArray> = Result.success(ByteArray(512 * 512 * 4) { 0xAB.toByte() }),
    private val releaseResult: Result<Unit> = Result.success(Unit),
) : StableDiffusionServicePort {
    override suspend fun initModel(modelPath: String, threads: Int) = initResult
    override suspend fun txt2img(prompt: String, negativePrompt: String?, width: Int, height: Int, steps: Int, cfgScale: Float, seed: Long) = txt2imgResult
    override suspend fun img2img(initImageRgba: ByteArray, initImageW: Int, initImageH: Int, prompt: String, negativePrompt: String?, width: Int, height: Int, steps: Int, cfgScale: Float, strength: Float, seed: Long) = img2imgResult
    override suspend fun release() = releaseResult
}
