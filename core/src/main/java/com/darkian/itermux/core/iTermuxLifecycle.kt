package com.darkian.itermux.core

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CopyOnWriteArraySet

// INTERNAL-TERMUX MODIFIED - merge carefully

interface iTermuxRuntimeLifecycleListener {
    fun onBootstrapState(
        state: iTermuxBootstrapState,
        cause: iTermuxRuntimeFailureCause?,
    )

    fun onSessionState(
        sessionId: String,
        state: iTermuxSessionState,
    )

    fun onEnvironmentValidation(
        result: iTermuxEnvironmentValidationResult,
        cause: iTermuxDegradedCause?,
    )
}

enum class iTermuxLifecycleCallbackThread {
    MAIN,
    CALLER,
}

object iTermuxLifecycleRegistry {
    private val listeners = CopyOnWriteArraySet<iTermuxRuntimeLifecycleListener>()

    @Volatile
    private var callbackThread: iTermuxLifecycleCallbackThread = iTermuxLifecycleCallbackThread.MAIN

    fun configure(callbackThread: iTermuxLifecycleCallbackThread) {
        this.callbackThread = callbackThread
    }

    fun addListener(listener: iTermuxRuntimeLifecycleListener) {
        listeners += listener
    }

    fun removeListener(listener: iTermuxRuntimeLifecycleListener) {
        listeners -= listener
    }

    fun dispatchBootstrapState(
        state: iTermuxBootstrapState,
        cause: iTermuxRuntimeFailureCause?,
    ) {
        dispatch {
            listeners.forEach { listener ->
                listener.onBootstrapState(state, cause)
            }
        }
    }

    fun dispatchSessionState(
        sessionId: String,
        state: iTermuxSessionState,
    ) {
        dispatch {
            listeners.forEach { listener ->
                listener.onSessionState(sessionId, state)
            }
        }
    }

    fun dispatchEnvironmentValidation(
        result: iTermuxEnvironmentValidationResult,
        cause: iTermuxDegradedCause?,
    ) {
        dispatch {
            listeners.forEach { listener ->
                listener.onEnvironmentValidation(result, cause)
            }
        }
    }

    private fun dispatch(block: () -> Unit) {
        if (callbackThread == iTermuxLifecycleCallbackThread.CALLER) {
            block()
            return
        }

        val mainLooper = runCatching { Looper.getMainLooper() }.getOrNull()
        if (mainLooper == null) {
            block()
            return
        }

        if (mainLooper.thread == Thread.currentThread()) {
            block()
            return
        }

        Handler(mainLooper).post(block)
    }
}
