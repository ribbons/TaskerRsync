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
import com.nerdoftheherd.tasker.rsync.config.DbclientConfig
import com.nerdoftheherd.tasker.rsync.output.CommandOutput
import java.io.BufferedReader

class DbclientRunner : TaskerPluginRunnerAction<DbclientConfig, CommandOutput>() {
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

        ProcessEnv(context, builder, input.regular.knownHosts).use {
            val dbclient = builder.start()

            val result = dbclient.waitFor()
            val stdout = dbclient.inputStream.bufferedReader().use(BufferedReader::readText)
            val stderr = dbclient.errorStream.bufferedReader().use(BufferedReader::readText)

            Log.d(TAG, "Run completed, exit code $result")

            if (result == 0) {
                return TaskerPluginResultSucess(CommandOutput(stdout, stderr))
            }

            throw RuntimeException(stderr)
        }
    }
}
