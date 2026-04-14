package com.darkian.itermux.sample

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.darkian.itermux.core.iTermux

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val runtimePaths = iTermux.initialize(this)
        val message = buildString {
            append("internal-termux sample host\n\n")
            append("This is scaffold-only for now.\n")
            append("filesDir: ")
            append(runtimePaths.filesDir)
            append("\nprefixDir: ")
            append(runtimePaths.prefixDir)
            append("\nhomeDir: ")
            append(runtimePaths.homeDir)
        }

        setContentView(
            TextView(this).apply {
                text = message
                textSize = 16f
                setPadding(48, 48, 48, 48)
            }
        )
    }
}
