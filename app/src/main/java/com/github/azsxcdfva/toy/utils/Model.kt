package com.github.azsxcdfva.toy.utils

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.RecognitionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun DigitalInkRecognizer.process(ink: Ink): RecognitionResult {
    return withContext(Dispatchers.Default) {
        suspendCoroutine { ctx ->
            recognize(ink)
                .addOnSuccessListener {
                    ctx.resume(it)
                }
                .addOnFailureListener {
                    ctx.resumeWithException(it)
                }
        }
    }
}

suspend fun RemoteModelManager.fetch(model: DigitalInkRecognitionModel) {
    return withContext(Dispatchers.Default) {
        suspendCoroutine { ctx ->
            download(model, DownloadConditions.Builder().build())
                .addOnSuccessListener {
                    ctx.resume(Unit)
                }
                .addOnFailureListener {
                    ctx.resumeWithException(it)
                }
        }
    }
}