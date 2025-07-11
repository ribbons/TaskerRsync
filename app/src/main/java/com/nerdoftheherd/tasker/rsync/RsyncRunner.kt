/*
 * Copyright © 2021-2025 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultErrorWithOutput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.nerdoftheherd.tasker.rsync.config.RsyncConfig
import com.nerdoftheherd.tasker.rsync.output.CommandOutput
import java.io.File

class RsyncRunner(private val timeoutOverride: Int? = null) :
    TaskerPluginRunnerAction<RsyncConfig, CommandOutput>() {
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

    private fun checkForMissingPermission(
        context: Context,
        args: RsyncArgExtractor,
    ): TaskerPluginResult<CommandOutput>? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return null
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (context.checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }
        } else {
            if (Environment.isExternalStorageManager()) {
                return null
            }
        }

        val externalPaths = ArrayList<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val manager =
                context.getSystemService(
                    Context.STORAGE_SERVICE,
                ) as StorageManager

            externalPaths +=
                manager.storageVolumes.mapNotNull {
                    it.directory?.absolutePath
                }
        } else {
            externalPaths +=
                Environment.getExternalStorageDirectory()
                    .absolutePath
        }

        // If the legacy /sdcard symlink points to an external storage
        // location add it to the list of prefixes to check too
        @SuppressLint("SdCardPath")
        if (externalPaths.contains(File("/sdcard").canonicalPath)) {
            externalPaths += "/sdcard"
        }

        args.paths.forEach { path ->
            if (externalPaths.any { path.startsWith(it) }) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    return TaskerPluginResultErrorWithOutput(
                        Utils.ERROR_MISSING_STORAGE_PERMISSION,
                        context.getString(
                            R.string.missing_storage_permission,
                            path,
                        ),
                    )
                } else {
                    return TaskerPluginResultErrorWithOutput(
                        Utils.ERROR_MISSING_STORAGE_PERMISSION,
                        context.getString(
                            R.string.missing_legacy_storage_permission,
                            path,
                        ),
                    )
                }
            }
        }

        return null
    }

    override fun run(
        context: Context,
        input: TaskerInput<RsyncConfig>,
    ): TaskerPluginResult<CommandOutput> {
        if (input.regular.checkForUpdates != false) {
            UpdateNotifier.checkInBackground(context)
        }

        val libDir = context.applicationInfo.nativeLibraryDir
        val parsedArgs = ArgumentParser.parse(input.regular.args)
        val argExtractor = RsyncArgExtractor(parsedArgs)

        if (argExtractor.remoteSrcOrDest &&
            !Utils.privateKeyFile(context).exists()
        ) {
            return TaskerPluginResultErrorWithOutput(
                Utils.ERROR_NO_PRIVATE_KEY,
                context.getString(R.string.no_private_key),
            )
        }

        checkForMissingPermission(context, argExtractor)?.let { error ->
            return error
        }

        Log.d(TAG, "About to run rsync")

        val args = ArrayList<String>()
        args.add("$libDir/librsync.so")
        args.addAll(parsedArgs)

        val builder = ProcessBuilder(args)
        val timeoutMS = timeoutOverride ?: this.requestedTimeout ?: 0

        ProcessEnv(context, builder, input.regular.knownHosts).use {
            val handler = ProcessHandler(context, builder, timeoutMS)
            val result = handler.run()

            if (result == 0) {
                return TaskerPluginResultSucess(
                    CommandOutput(
                        handler.stdout.toString(),
                        handler.stderr.toString(),
                    ),
                )
            }

            return TaskerPluginResultErrorWithOutput(
                result,
                handler.stderr.toString(),
            )
        }
    }
}
