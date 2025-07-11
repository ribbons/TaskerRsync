/*
 * Copyright © 2021-2024 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultErrorWithOutput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.nerdoftheherd.tasker.rsync.output.PublicKeyOutput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PublicKeyRunnerTest {
    @Test
    fun noPrivateKey() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val keyFile = File(context.filesDir, "id_dropbear")
        keyFile.delete()

        val output = PublicKeyRunner().run(context, TaskerInput(Unit))
        assertFalse(output.success)

        val error = output as TaskerPluginResultErrorWithOutput<PublicKeyOutput>
        assertEquals(context.getString(R.string.no_private_key), error.message)
    }

    @Test
    fun errorFromFailure() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        File(context.filesDir, "id_dropbear").createNewFile()

        val output = PublicKeyRunner().run(context, TaskerInput(Unit))
        assertFalse(output.success)

        val error = output as TaskerPluginResultErrorWithOutput<PublicKeyOutput>
        assertEquals("Exited: Bad buf_getptr\n", error.message)
    }

    @Test
    fun pubkeyFromPrivate() {
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val context = ApplicationProvider.getApplicationContext<Context>()

        File(context.filesDir, "id_dropbear").outputStream().use { fileOut ->
            assets.open("private_key_ed25519").copyTo(fileOut)
        }

        val result = PublicKeyRunner().run(context, TaskerInput(Unit))
        assertTrue(result.success)

        val output = result as TaskerPluginResultSucess<PublicKeyOutput>
        assertEquals(
            "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIIhSZ6CSqV74qSOMgd3dZOGufal" +
                "53zMe1CVTEpdyXrMY rsync-for-tasker@android",
            output.regular?.pubkey,
        )
    }
}
