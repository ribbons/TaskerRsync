/*
 * Copyright © 2021-2025 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultErrorWithOutput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.nerdoftheherd.tasker.rsync.output.PublicKeyOutput
import java.util.Scanner

class PublicKeyRunner : TaskerPluginRunnerActionNoInput<PublicKeyOutput>() {
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
        input: TaskerInput<Unit>,
    ): TaskerPluginResult<PublicKeyOutput> {
        val libDir = context.applicationInfo.nativeLibraryDir
        val privateKey = Utils.privateKeyFile(context)

        if (!privateKey.exists()) {
            return TaskerPluginResultErrorWithOutput(
                Utils.ERROR_NO_PRIVATE_KEY,
                context.getString(R.string.no_private_key),
            )
        }

        Log.d(TAG, "About to run dropbearkey")

        val builder =
            ProcessBuilder(
                arrayListOf(
                    "$libDir/libdropbearkey.so",
                    "-f",
                    privateKey.absolutePath,
                    "-y",
                ),
            )

        val timeoutMS = this.requestedTimeout ?: 0
        val handler = ProcessHandler(context, builder, timeoutMS)
        val result = handler.run()

        if (result != 0) {
            return TaskerPluginResultErrorWithOutput(
                result,
                handler.stderr.toString(),
            )
        }

        val scanner = Scanner(handler.stdout.toString())
        val pubkey =
            scanner.findWithinHorizon(
                "(?<=\n)ssh-[a-z0-9]+ [a-zA-Z0-9+/]+={0,2} ",
                0,
            )

        if (pubkey != null) {
            return TaskerPluginResultSucess(
                PublicKeyOutput("${pubkey}rsync-for-tasker@android"),
            )
        }

        throw RuntimeException(
            "Unable to find public key in dropbearkey output",
        )
    }
}
