/*
 * Copyright © 2021-2022 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.content.Context
import android.os.Build
import android.system.Os
import java.io.Closeable
import java.io.File
import java.util.UUID

class ProcessEnv constructor(
    context: Context,
    builder: ProcessBuilder,
    knownHosts: String?
) : Closeable {
    val home = File(context.cacheDir, UUID.randomUUID().toString())
    val pathDir = File(context.cacheDir, UUID.randomUUID().toString())

    init {
        home.mkdir()
        pathDir.mkdir()

        val sshDir = File(home, ".ssh").apply {
            this.mkdir()
        }

        val srcKey = Utils.privateKeyFile(context)

        if (srcKey.exists()) {
            srcKey.copyTo(File(sshDir, "id_dropbear"))
        }

        if (knownHosts != null) {
            val knownHostsFile = File(sshDir, "known_hosts")
            knownHostsFile.writeText(knownHosts)
        }

        val target = "${context.applicationInfo.nativeLibraryDir}/libdbclient.so"
        val linkname = "${pathDir.path}/ssh"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Os.symlink(target, linkname)
        } else {
            val ln = ProcessBuilder("ln", "-s", target, linkname)
            ln.start().waitFor()
        }

        val env = builder.environment()
        env["HOME"] = home.path
        env["PATH"] = "${pathDir.path}:${env["PATH"]}"
    }

    override fun close() {
        home.deleteRecursively()
        pathDir.deleteRecursively()
    }
}
