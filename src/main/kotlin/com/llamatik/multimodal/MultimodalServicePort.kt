package com.llamatik.multimodal

import com.llamatik.library.platform.GenStream

interface MultimodalServicePort {
    suspend fun initModel(modelPath: String, mmprojPath: String): Result<Boolean>
    fun analyzeImageBytesStream(imageBytes: ByteArray, prompt: String, callback: GenStream)
    suspend fun cancelAnalysis(): Result<Unit>
    suspend fun release(): Result<Unit>
}
