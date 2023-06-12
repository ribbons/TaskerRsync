/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.nerdoftheherd.tasker.rsync.config.DbclientConfig
import com.nerdoftheherd.tasker.rsync.output.CommandOutput
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class DbclientRunner : TaskerPluginRunnerAction<DbclientConfig, CommandOutput>() {
    override val notificationProperties get() = NotificationProperties(
        iconResId = R.drawable.ic_notification
    ) { context ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setColor(ContextCompat.getColor(context, R.color.primary))
        } else {
            this
        }
    }

    override fun run(context: Context, input: TaskerInput<DbclientConfig>): TaskerPluginResult<CommandOutput> {
        if (input.regular.checkForUpdates) {
            UpdateNotifier.checkInBackground(context)
        }

        val libDir = context.applicationInfo.nativeLibraryDir

        if (!Utils.privateKeyFile(context).exists()) {
            throw java.lang.RuntimeException(context.getString(R.string.no_private_key))
        }

        Log.d(TAG, "About to run dbclient")

        val args = ArrayList<String>()
        args.add("$libDir/libdbclient.so")
        args.addAll(ArgumentParser.parse(input.regular.args))

        val builder = ProcessBuilder(args)
        val stdout = StringBuilder()
        val stderr = StringBuilder()

        ProcessEnv(context, builder, input.regular.knownHosts).use {
            val dbclient = builder.start()

            thread {
                dbclient.inputStream.bufferedReader().use {
                    while (true) {
                        try {
                            val line = it.readLine() ?: break
                            stdout.appendLine(line)
                        } catch (_: IOException) {
                            break
                        }
                    }
                }
            }

            thread {
                dbclient.errorStream.bufferedReader().use {
                    while (true) {
                        try {
                            val line = it.readLine() ?: break
                            stderr.appendLine(line)
                        } catch (_: IOException) {
                            break
                        }
                    }
                }
            }

            if (input.regular.timeoutSeconds > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!dbclient.waitFor(input.regular.timeoutSeconds.toLong(), TimeUnit.SECONDS)) {
                        dbclient.destroyForcibly()
                    }
                } else {
                    val end = System.currentTimeMillis() + input.regular.timeoutSeconds * 1000

                    while (end > System.currentTimeMillis()) {
                        try {
                            dbclient.exitValue()
                            break
                        } catch (_: IllegalThreadStateException) { }

                        Thread.sleep(250)
                    }

                    if (end < System.currentTimeMillis()) {
                        dbclient.destroy()
                    }
                }
            }

            val result = dbclient.waitFor()

            Log.d(TAG, "Run completed, exit code $result")

            if (result == 0) {
                return TaskerPluginResultSucess(CommandOutput(stdout.toString(), stderr.toString()))
            }

            throw RuntimeException(stderr.toString())
        }
    }
}
