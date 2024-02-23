/*
 * Copyright © 2021-2024 Matt Robinson
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
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultErrorWithOutput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.nerdoftheherd.tasker.rsync.config.RsyncConfig
import com.nerdoftheherd.tasker.rsync.output.CommandOutput
import java.io.BufferedReader

class RsyncRunner : TaskerPluginRunnerAction<RsyncConfig, CommandOutput>() {
    override val notificationProperties get() =
        NotificationProperties(
            iconResId = R.drawable.ic_notification,
        ) { context ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setColor(ContextCompat.getColor(context, R.color.primary))
            } else {
                this
            }
        }

    override fun run(
        context: Context,
        input: TaskerInput<RsyncConfig>,
    ): TaskerPluginResult<CommandOutput> {
        if (input.regular.checkForUpdates) {
            UpdateNotifier.checkInBackground(context)
        }

        val libDir = context.applicationInfo.nativeLibraryDir

        if (!Utils.privateKeyFile(context).exists()) {
            return TaskerPluginResultErrorWithOutput(
                Utils.ERROR_NO_PRIVATE_KEY,
                context.getString(R.string.no_private_key),
            )
        }

        Log.d(TAG, "About to run rsync")

        val args = ArrayList<String>()
        args.add("$libDir/librsync.so")
        args.addAll(ArgumentParser.parse(input.regular.args))

        val builder = ProcessBuilder(args)

        ProcessEnv(context, builder, input.regular.knownHosts).use {
            val rsync = builder.start()

            val result = rsync.waitFor()
            val stdout = rsync.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderr = rsync.errorStream.bufferedReader().use(BufferedReader::readText)

            Log.d(TAG, "Run completed, exit code $result")

            if (result == 0) {
                return TaskerPluginResultSucess(CommandOutput(stdout, stderr))
            }

            return TaskerPluginResultErrorWithOutput(result, stderr)
        }
    }
}
