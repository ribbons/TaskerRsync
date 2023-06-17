/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.nerdoftheherd.tasker.rsync.config.DbclientConfig
import com.nerdoftheherd.tasker.rsync.output.CommandOutput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class DbclientRunnerTest {
    @Test
    fun noPrivateKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = DbclientConfig("-h", "", 0, false)

        val keyFile = File(context.filesDir, "id_dropbear")
        keyFile.delete()

        val exp = assertThrows(RuntimeException::class.java) {
            DbclientRunner().run(context, TaskerInput(config))
        }

        assertEquals(context.getString(R.string.no_private_key), exp.message)
    }

    @Test
    fun errorFromFailure() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = DbclientConfig("localhost", "", 0, false)

        File(context.filesDir, "id_dropbear").createNewFile()

        assertThrows(RuntimeException::class.java) {
            DbclientRunner().run(context, TaskerInput(config))
        }
    }

    @Test
    fun successFromNormalExit() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = DbclientConfig("-h", "", 0, false)

        File(context.filesDir, "id_dropbear").createNewFile()

        val output = DbclientRunner().run(context, TaskerInput(config))
        assertTrue(output.success)
    }

    @Test
    fun captureStderr() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = DbclientConfig("-h", "", 0, false)

        File(context.filesDir, "id_dropbear").createNewFile()

        val output = DbclientRunner().run(context, TaskerInput(config))
        val outputSuccess = output as TaskerPluginResultSucess<CommandOutput>
        val stderr = outputSuccess.regular?.stderr!!

        assertTrue(stderr.startsWith("Dropbear SSH client v20"))
        assertTrue(stderr.contains("-V    Version\n"))
    }

    @Test(timeout = 1500)
    fun errorFromTimeout() {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = DbclientConfig("test@example.com", "", 1, false)

        File(context.filesDir, "id_dropbear").outputStream().use { fileOut ->
            assets.open("private_key_ed25519").copyTo(fileOut)
        }

        assertThrows(RuntimeException::class.java) {
            DbclientRunner().run(context, TaskerInput(config))
        }
    }
}
