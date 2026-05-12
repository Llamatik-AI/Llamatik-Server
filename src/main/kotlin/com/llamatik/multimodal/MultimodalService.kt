package com.llamatik.multimodal

import com.llamatik.library.platform.GenStream
import com.llamatik.library.platform.MultimodalBridge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object MultimodalService {

    private val mutex = Mutex()

    suspend fun initModel(modelPath: String, mmprojPath: String): Result<Boolean> = runCatching {
        mutex.withLock { MultimodalBridge.initModel(modelPath, mmprojPath) }
    }

    fun analyzeImageBytesStream(imageBytes: ByteArray, prompt: String, callback: GenStream) {
        MultimodalBridge.analyzeImageBytesStream(imageBytes, prompt, callback)
    }

    suspend fun cancelAnalysis(): Result<Unit> = runCatching {
        mutex.withLock { MultimodalBridge.cancelAnalysis() }
    }

    suspend fun release(): Result<Unit> = runCatching {
        mutex.withLock { MultimodalBridge.release() }
    }
}
