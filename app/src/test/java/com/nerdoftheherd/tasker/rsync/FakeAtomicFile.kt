/*
 * Copyright © 2022 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.util.AtomicFile
import java.io.File
import java.io.FileOutputStream

class FakeAtomicFile(
    private val baseFile: File,
) : AtomicFile(baseFile) {
    override fun getBaseFile(): File = baseFile

    override fun readFully(): ByteArray = baseFile.readBytes()

    override fun startWrite(): FileOutputStream = baseFile.outputStream()

    override fun finishWrite(str: FileOutputStream?) {
        str?.flush()
        str?.close()
    }
}
