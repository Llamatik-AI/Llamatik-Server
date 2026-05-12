package com.llamatik.whisper

import com.llamatik.library.platform.WhisperBridge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object WhisperService {

    private val mutex = Mutex()

    suspend fun initModel(modelPath: String): Result<Boolean> = runCatching {
        mutex.withLock { WhisperBridge.initModel(modelPath) }
    }

    suspend fun transcribeWav(wavPath: String, language: String?, initialPrompt: String?): Result<String> =
        runCatching {
            mutex.withLock { WhisperBridge.transcribeWav(wavPath, language, initialPrompt) }
        }

    suspend fun release(): Result<Unit> = runCatching {
        mutex.withLock { WhisperBridge.release() }
    }
}
