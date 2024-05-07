/*
 * Copyright © 2021-2024 Matt Robinson
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

            return Environment.getExternalStorageDirectory()
        }

        fun setExternalStoragePermission(
            context: Context,
            enabled: Boolean,
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val permAllowed = fun(): Boolean {
                    return appOps.unsafeCheckOpNoThrow(
                        "android:manage_external_storage",
                        Process.myUid(),
                        context.packageName,
                    ) == AppOpsManager.MODE_ALLOWED
                }

                if (permAllowed() == enabled) {
                    return
                }

                InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                    "appops set ${context.packageName} MANAGE_EXTERNAL_STORAGE " +
                        if (enabled) "allow" else "deny",
                )

                while (permAllowed() != enabled) {
                    Thread.sleep(1)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (enabled) {
                    InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                        context.packageName,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                } else {
                    InstrumentationRegistry.getInstrumentation().uiAutomation.revokeRuntimePermission(
                        context.packageName,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                }
            }
        }
    }
}
