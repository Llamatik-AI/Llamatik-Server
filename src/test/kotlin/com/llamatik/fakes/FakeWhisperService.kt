package com.llamatik.fakes

import com.llamatik.whisper.WhisperServicePort

class FakeWhisperService(
    private val initResult: Result<Boolean> = Result.success(true),
    private val transcribeResult: Result<String> = Result.success("hello world"),
    private val releaseResult: Result<Unit> = Result.success(Unit),
) : WhisperServicePort {
    override suspend fun initModel(modelPath: String) = initResult
    override suspend fun transcribeWav(wavPath: String, language: String?, initialPrompt: String?) = transcribeResult
    override suspend fun release() = releaseResult
}
