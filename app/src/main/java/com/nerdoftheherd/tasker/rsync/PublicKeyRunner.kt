/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import android.util.Log
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.nerdoftheherd.tasker.rsync.output.PublicKeyOutput
import java.io.BufferedReader
import java.util.Scanner

class PublicKeyRunner : TaskerPluginRunnerActionNoInput<PublicKeyOutput>() {
    override fun run(
        context: Context,
        input: TaskerInput<Unit>
    ): TaskerPluginResult<PublicKeyOutput> {
        val libDir = context.applicationInfo.nativeLibraryDir
        val privateKey = Utils.privateKeyFile(context)

        if (!privateKey.exists()) {
            throw java.lang.RuntimeException(context.getString(R.string.no_private_key))
        }

        Log.d(TAG, "About to run dropbearkey")

        val dropbearkey = Runtime.getRuntime().exec(
            arrayOf(
                "$libDir/libdropbearkey.so",
                "-f", privateKey.absolutePath,
                "-y",
            )
        )

        val retcode = dropbearkey.waitFor()
        Log.d(TAG, "Completed, exit code $retcode")

        if (retcode != 0) {
            throw RuntimeException(
                dropbearkey.errorStream.bufferedReader().use(BufferedReader::readText)
            )
        }

        val scanner = Scanner(dropbearkey.inputStream)
        val pubkey = scanner.findWithinHorizon("(?<=\n)ssh-[a-z0-9]+ [a-zA-Z0-9+/]+={0,2} ", 0)

        if (pubkey != null) {
            return TaskerPluginResultSucess(
                PublicKeyOutput("${pubkey}rsync-for-tasker@android")
            )
        }

        throw RuntimeException("Unable to find public key in dropbearkey output")
    }
}
