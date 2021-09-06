/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import java.io.Closeable
import java.io.File
import java.util.UUID

class ProcessEnv constructor(context: Context, private val builder: ProcessBuilder) : Closeable {
    val home = File(context.cacheDir, UUID.randomUUID().toString())

    init {
        home.mkdir()

        val sshDir = File(home, ".ssh").apply {
            this.mkdir()
        }

        val srcKey = Utils.privateKeyFile(context)

        if (srcKey.exists()) {
            srcKey.copyTo(File(sshDir, "id_dropbear"))
        }

        val env = builder.environment()
        env["HOME"] = home.path
    }

    override fun close() {
        home.deleteRecursively()
    }
}
