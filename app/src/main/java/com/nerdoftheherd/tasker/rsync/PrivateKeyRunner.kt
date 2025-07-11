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
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.nerdoftheherd.tasker.rsync.config.PrivateKeyConfig
import java.io.BufferedReader
import java.io.File

class PrivateKeyRunner : TaskerPluginRunnerActionNoOutput<PrivateKeyConfig>() {
    override val notificationProperties get() =
        NotificationProperties(
            iconResId = R.drawable.ic_notification,
        ) { context ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                setColor(ContextCompat.getColor(context, R.color.primary))
            } else {
                setColor(context.getColor(R.color.primary))
            }
        }

    override fun run(
        context: Context,
        input: TaskerInput<PrivateKeyConfig>,
    ): TaskerPluginResult<Unit> {
        val libDir = context.applicationInfo.nativeLibraryDir
        val privateKey = Utils.privateKeyFile(context)

        if (privateKey.exists()) {
            if (input.regular.overwrite) {
                privateKey.delete()
            } else {
                return TaskerPluginResultError(
                    R.string.key_exists,
                    context.getString(R.string.key_exists),
                )
            }
        }

        Log.d(TAG, "About to run dropbearkey")

        val dropbearkey =
            Runtime.getRuntime().exec(
                arrayOf(
                    "$libDir/libdropbearkey.so",
                    "-t",
                    input.regular.keyType.lowercase(),
                    "-s",
                    input.regular.keySize.toString(),
                    "-f",
                    privateKey.absolutePath,
                ),
            )

        val retcode = dropbearkey.waitFor()
        Log.d(TAG, "Completed, exit code $retcode")
        File(context.filesDir, "${Utils.KEY_FILENAME}.pub").delete()

        return if (retcode == 0) {
            TaskerPluginResultSucess()
        } else {
            TaskerPluginResultError(
                retcode,
                dropbearkey.errorStream.bufferedReader().use(
                    BufferedReader::readText,
                ),
            )
        }
    }
}
