/*
 * Copyright © 2021-2026 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import android.util.Log
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultErrorWithOutput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.nerdoftheherd.tasker.rsync.config.DbclientConfig
import com.nerdoftheherd.tasker.rsync.output.CommandOutput

class DbclientRunner(
    private val timeoutOverride: Int? = null,
) : TaskerPluginRunnerAction<DbclientConfig, CommandOutput>() {
    override val notificationProperties get() =
        NotificationProperties(
            iconResId = R.drawable.ic_notification,
        ) { context -> setColor(context.getColor(R.color.primary)) }

    override fun run(
        context: Context,
        input: TaskerInput<DbclientConfig>,
    ): TaskerPluginResult<CommandOutput> {
        if (input.regular.checkForUpdates != false) {
            UpdateNotifier.checkInBackground(context)
        }

        val binpath =
            context.applicationInfo.nativeLibraryDir + "/libdbclient.so"

        if (!Utils.privateKeyFile(context).exists()) {
            return TaskerPluginResultErrorWithOutput(
                Utils.ERROR_NO_PRIVATE_KEY,
                context.getString(R.string.no_private_key),
            )
        }

        Log.d(TAG, "About to run dbclient")

        val args = ArrayList<String>()
        args.add(binpath)
        args.addAll(ArgumentParser.parse(input.regular.args))

        val builder = ProcessBuilder(args)
        val timeoutMS = timeoutOverride ?: this.requestedTimeout ?: 0

        ProcessEnv(context, builder, input.regular.knownHosts).use {
            val handler = ProcessHandler(context, builder, timeoutMS)
            val result = handler.run()
            val stderr = handler.stderr.toString().replace(binpath, "dbclient")

            if (result == 0) {
                return TaskerPluginResultSucess(
                    CommandOutput(handler.stdout.toString(), stderr),
                )
            }

            return TaskerPluginResultErrorWithOutput(result, stderr)
        }
    }
}
