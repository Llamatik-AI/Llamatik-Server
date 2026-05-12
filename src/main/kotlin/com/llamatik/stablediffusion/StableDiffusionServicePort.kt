package com.llamatik.stablediffusion

interface StableDiffusionServicePort {
    suspend fun initModel(modelPath: String, threads: Int): Result<Boolean>
    suspend fun txt2img(prompt: String, negativePrompt: String?, width: Int, height: Int, steps: Int, cfgScale: Float, seed: Long): Result<ByteArray>
    suspend fun img2img(initImageRgba: ByteArray, initImageW: Int, initImageH: Int, prompt: String, negativePrompt: String?, width: Int, height: Int, steps: Int, cfgScale: Float, strength: Float, seed: Long): Result<ByteArray>
    suspend fun release(): Result<Unit>
}
