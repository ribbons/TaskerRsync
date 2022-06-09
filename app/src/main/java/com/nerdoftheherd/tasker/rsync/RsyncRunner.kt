/*
 * Copyright © 2021-2022 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import android.util.Log
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.nerdoftheherd.tasker.rsync.config.RsyncConfig
import com.nerdoftheherd.tasker.rsync.output.CommandOutput
import java.io.BufferedReader

class RsyncRunner : TaskerPluginRunnerAction<RsyncConfig, CommandOutput>() {
    override fun run(context: Context, input: TaskerInput<RsyncConfig>): TaskerPluginResult<CommandOutput> {
        UpdateNotifier.checkInBackground(context)

        val libDir = context.applicationInfo.nativeLibraryDir

        if (!Utils.privateKeyFile(context).exists()) {
            throw java.lang.RuntimeException(context.getString(R.string.no_private_key))
        }

        Log.d(TAG, "About to run rsync")

        val args = ArrayList<String>()
        args.add("$libDir/librsync.so")
        args.addAll(ArgumentParser.parse(input.regular.args))

        val builder = ProcessBuilder(args)

        ProcessEnv(context, builder).use {
            val rsync = builder.start()

            val result = rsync.waitFor()
            val stdout = rsync.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderr = rsync.errorStream.bufferedReader().use(BufferedReader::readText)

            Log.d(TAG, "Run completed, exit code $result")

            if (result == 0) {
                return TaskerPluginResultSucess(CommandOutput(stdout, stderr))
            }

            throw RuntimeException(stderr)
        }
    }
}
