package com.github.azsxcdfva.toy.utils

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.suspendCoroutine

private val requestKeyPool = AtomicInteger(0)

class ActivityResultLifecycle : LifecycleOwner {
    private val lifecycle = LifecycleRegistry(this)

    init {
        lifecycle.currentState = Lifecycle.State.INITIALIZED
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycle
    }

    suspend fun <T> use(block: suspend (lifecycle: ActivityResultLifecycle, start: () -> Unit) -> T): T {
        return try {
            markCreated()

            block(this, this::markStarted)
        } finally {
            withContext(NonCancellable) {
                markDestroy()
            }
        }
    }

    private fun markCreated() {
        lifecycle.currentState = Lifecycle.State.CREATED
    }

    private fun markStarted() {
        lifecycle.currentState = Lifecycle.State.STARTED
        lifecycle.currentState = Lifecycle.State.RESUMED
    }

    private fun markDestroy() {
        lifecycle.currentState = Lifecycle.State.DESTROYED
    }
}

suspend fun <I, O> ComponentActivity.startActivityForResult(
    contracts: ActivityResultContract<I, O>,
    input: I,
): O {
    val requestKey = (requestKeyPool.addAndGet(1) % 0xffff) + 1

    return ActivityResultLifecycle().use { lifecycle, start ->
        suspendCoroutine { ctx ->
            activityResultRegistry.register(requestKey.toString(), lifecycle, contracts) {
                ctx.resumeWith(Result.success(it))
            }.apply { start() }.launch(input)
        }
    }
}