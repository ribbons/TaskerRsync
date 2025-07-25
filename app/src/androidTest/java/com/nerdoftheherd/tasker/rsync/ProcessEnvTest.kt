/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProcessEnvTest {
    @Test
    fun createsDirectoryForHome() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        ProcessEnv(context, ProcessBuilder("test"), "").use { env ->
            assertTrue(env.home.exists())
        }
    }

    @Test
    fun copiesPrivateKeyToEnvHome() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Utils.privateKeyFile(context).createNewFile()

        ProcessEnv(context, ProcessBuilder("test"), "").use { env ->
            val sshDir = File(env.home, ".ssh")
            assertTrue(File(sshDir, "id_dropbear").exists())
        }
    }

    @Test
    fun doesNotThrowIfKeyMissing() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Utils.privateKeyFile(context).delete()

        ProcessEnv(context, ProcessBuilder("test"), "").use {}
    }

    @Test
    fun writesKnownHostsToEnvHome() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val knownHosts =
            "example.org ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIMBh0B+Tfy" +
                "/wEJWyejoOHclat7UPGKTqKey+rfVQr94I"

        ProcessEnv(context, ProcessBuilder("test"), knownHosts).use { env ->
            val sshDir = File(env.home, ".ssh")
            val content = File(sshDir, "known_hosts").readText()
            assertEquals(knownHosts, content)
        }
    }

    @Test
    fun allowsNullKnownHosts() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ProcessEnv(context, ProcessBuilder("test"), null).use { }
    }

    @Test
    fun removesHomeDirOnClose() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var homeDir: File

        ProcessEnv(context, ProcessBuilder("test"), "").use { env ->
            File(env.home, "newfile").createNewFile()
            homeDir = env.home
        }

        assertFalse(homeDir.exists())
    }

    @Test
    fun setsHomeCorrectly() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val builder = ProcessBuilder("test")

        ProcessEnv(context, builder, "").use { env ->
            assertEquals(env.home.absolutePath, builder.environment()["HOME"])
        }
    }

    @Test
    fun sshExecutableInPath() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val builder = ProcessBuilder("sh", "-c", "ssh")

        ProcessEnv(context, builder, "").use {
            val ssh = builder.start()
            assertEquals(1, ssh.waitFor())
        }
    }

    @Test
    fun removesPathDirOnClose() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var pathDir: File

        ProcessEnv(context, ProcessBuilder("test"), "").use { env ->
            pathDir = env.pathDir
        }

        assertFalse(pathDir.exists())
    }
}
