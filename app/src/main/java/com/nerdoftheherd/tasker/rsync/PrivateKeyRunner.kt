/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import android.util.Log
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import java.io.BufferedReader
import java.io.File

class PrivateKeyRunner : TaskerPluginRunnerActionNoOutput<PrivateKeyConfig>() {
    override fun run(context: Context, input: TaskerInput<PrivateKeyConfig>): TaskerPluginResult<Unit> {
        val libDir = context.applicationInfo.nativeLibraryDir
        val privateKey = File(context.filesDir, "id_dropbear")

        if (privateKey.exists()) {
            if (input.regular.overwrite) {
                privateKey.delete()
            } else {
                return TaskerPluginResultError(
                    R.string.key_exists,
                    context.getString(R.string.key_exists)
                )
            }
        }

        Log.d(TAG, "About to run dropbearkey")

        val dropbearkey = Runtime.getRuntime().exec(
            arrayOf(
                "$libDir/libdropbearkey.so",
                "-t", input.regular.keyType.lowercase(),
                "-s", input.regular.keySize.toString(),
                "-f", privateKey.absolutePath
            )
        )

        val retcode = dropbearkey.waitFor()
        Log.d(TAG, "Completed, exit code $retcode")

        return if (retcode == 0) {
            TaskerPluginResultSucess()
        } else {
            TaskerPluginResultError(
                retcode,
                dropbearkey.errorStream.bufferedReader().use(BufferedReader::readText)
            )
        }
    }
}
