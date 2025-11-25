/*
 * Copyright © 2023-2025 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import java.io.IOException
import java.util.Scanner
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private const val TIMEOUT_MARGIN = 1000
private const val SIGTERM = 0xF

class ProcessHandler(
    private val context: Context,
    private val builder: ProcessBuilder,
    private val timeoutMS: Int,
) {
    val stdout = StringBuilder()
    val stderr = StringBuilder()

    private var aborted = false
    private var stdoutEnded = false
    private var stderrEnded = false

    fun run(): Int {
        val process = builder.start()

        thread {
            process.inputStream.bufferedReader().use {
                while (!aborted) {
                    try {
                        val line = it.readLine() ?: break
                        stdout.appendLine(line)
                    } catch (_: IOException) {
                        break
                    }
                }

                stdoutEnded = true
            }
        }

        thread {
            process.errorStream.bufferedReader().use {
                while (!aborted) {
                    try {
                        val line = it.readLine() ?: break
                        stderr.appendLine(line)
                    } catch (_: IOException) {
                        break
                    }
                }

                stderrEnded = true
            }
        }

        if (timeoutMS > TIMEOUT_MARGIN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!process.waitFor(
                        timeoutMS.toLong() - TIMEOUT_MARGIN,
                        TimeUnit.MILLISECONDS,
                    )
                ) {
                    process.destroy()
                    aborted = true
                }
            } else {
                val end =
                    System.currentTimeMillis() + timeoutMS - TIMEOUT_MARGIN

                while (end >= System.currentTimeMillis()) {
                    try {
                        process.exitValue()
                        break
                    } catch (_: IllegalThreadStateException) {
                    }

                    Thread.sleep(250)
                }

                if (end < System.currentTimeMillis()) {
                    // Pre-API 26, process.destroy() sends SIGKILL rather than
                    // SIGTERM, causing stream related hangs after killing
                    // rsync due to cleanup not running in the signal handler
                    // Process pid is not public but is returned from toString
                    val scanner = Scanner(process.toString())
                    val pid = scanner.findWithinHorizon("(?<=pid=)[0-9]+", 0)

                    if (pid != null) {
                        Process.sendSignal(pid.toInt(), SIGTERM)
                    } else {
                        process.destroy()
                    }

                    aborted = true
                }
            }
        }

        val result = process.waitFor()
        Log.d(TAG, "Process exit code $result")

        while (!stdoutEnded || !stderrEnded) {
            Thread.sleep(100)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O &&
            stderr
                .lines()
                .first()
                .endsWith(".so: unsupported flags DT_FLAGS_1=0x8000001")
        ) {
            stderr.delete(0, stderr.indexOf("\n") + 1)
        }

        if (aborted) {
            stderr.appendLine(
                context.getString(R.string.process_killed_timeout),
            )
        }

        return result
    }
}
