/*
 * Copyright © 2021 Matt Robinson
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
        Log.d(TAG, "About to run dbclient")
        val libDir = context.applicationInfo.nativeLibraryDir

        val args = ArrayList<String?>()
        args.add("$libDir/libdbclient.so")
        input.regular.args?.split(' ')?.let { args.addAll(it) }

        val dbclient = Runtime.getRuntime().exec(args.toTypedArray())

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
