/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.storage.StorageManager
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

class TestUtils {
    companion object {
        fun primaryStorageDir(context: Context): File {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val manager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                val volume = manager.primaryStorageVolume
                return volume.directory!!
            }

            // Fall back to using getExternalStorageDirectory even though it is
            // deprecated as it appears to be the only way of getting this
            // before Android 11
            @Suppress("DEPRECATION")
            return Environment.getExternalStorageDirectory()
        }

        fun setManageStoragePermission(
            context: Context,
            enabled: Boolean,
        ) {
            val newstate = if (enabled) "allow" else "deny"

            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                "appops set --uid ${context.packageName} MANAGE_EXTERNAL_STORAGE $newstate",
            )

            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

            while (
                appOps.unsafeCheckOpNoThrow(
                    "android:manage_external_storage",
                    Process.myUid(),
                    context.packageName,
                ) != AppOpsManager.MODE_ALLOWED == enabled
            ) {
                Thread.sleep(1)
            }
        }
    }
}
