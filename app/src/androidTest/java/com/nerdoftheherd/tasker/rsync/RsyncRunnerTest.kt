/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.nerdoftheherd.tasker.rsync.config.RsyncConfig
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class RsyncRunnerTest {
    @Rule @JvmField
    val expecter: ExpectedException = ExpectedException.none()

    @Test
    fun noPrivateKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = RsyncConfig("-h", "", false)

        val keyFile = File(context.filesDir, "id_dropbear")
        keyFile.delete()

        expecter.expect(RuntimeException::class.java)
        expecter.expectMessage(context.getString(R.string.no_private_key))
        RsyncRunner().run(context, TaskerInput(config))
    }

    @Test
    fun errorFromFailure() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = RsyncConfig("--invalid", "", false)

        expecter.expect(RuntimeException::class.java)
        RsyncRunner().run(context, TaskerInput(config))
    }

    @Test
    fun copyFromAndToPrimaryStorage() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            TestUtils.setManageStoragePermission(context, true)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
                context.packageName,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        File(context.filesDir, "id_dropbear").createNewFile()

        val pkgName = context.applicationContext.packageName
        val sourceDir = File(TestUtils.primaryStorageDir(context), "$pkgName-source")
        val targetDir = File(TestUtils.primaryStorageDir(context), "$pkgName-target")

        sourceDir.mkdir()
        File(sourceDir, "testfile").createNewFile()
        targetDir.deleteRecursively()

        val config = RsyncConfig(
            "-r ${sourceDir.absolutePath}/ ${targetDir.absolutePath}/",
            "",
            false
        )
        RsyncRunner().run(context, TaskerInput(config))

        assertTrue(File(targetDir, "testfile").exists())
        sourceDir.deleteRecursively()
        targetDir.deleteRecursively()
    }
}
