/*
 * Copyright © 2024-2025 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RsyncArgExtractorTest {
    @Test
    fun simpleSrcDest() {
        val extractor = RsyncArgExtractor(listOf("src", "dest"))
        assertEquals(listOf("src", "dest"), extractor.paths)
    }

    @Test
    fun multipleSrcDest() {
        val extractor = RsyncArgExtractor(listOf("src1", "src2", "dest"))
        assertEquals(listOf("src1", "src2", "dest"), extractor.paths)
    }

    @Test
    fun handleShortOpts() {
        val extractor = RsyncArgExtractor(listOf("-v", "-aqc", "src", "dest"))
        assertEquals(listOf("src", "dest"), extractor.paths)
    }

    @Test
    fun handleLongOpts() {
        val extractor =
            RsyncArgExtractor(
                listOf(
                    "--verbose",
                    "--chmod=ugo=rwX",
                    "src",
                    "dest",
                ),
            )

        assertEquals(listOf("src", "dest"), extractor.paths)
    }

    @Test
    fun pathsFromFileArgs() {
        val extractor =
            RsyncArgExtractor(
                listOf(
                    "--exclude-from=excludefile",
                    "--include-from=includefile",
                    "--files-from=fromfile",
                    "--log-file=logfile",
                    "--write-batch=writebatchfile",
                    "--only-write-batch=onlywritebatchfile",
                    "--read-batch=readbatchfile",
                    "src",
                    "dest",
                ),
            )

        assertEquals(
            listOf(
                "excludefile",
                "includefile",
                "fromfile",
                "logfile",
                "writebatchfile",
                "onlywritebatchfile",
                "readbatchfile",
                "src",
                "dest",
            ),
            extractor.paths,
        )
    }

    @Test
    fun pathsFromFileArgWithEquals() {
        val extractor =
            RsyncArgExtractor(
                listOf(
                    "--exclude-from=exclude=file",
                    "src",
                    "dest",
                ),
            )

        assertEquals(
            listOf("exclude=file", "src", "dest"),
            extractor.paths,
        )
    }

    @Test
    fun localSrcAndDest() {
        val extractor = RsyncArgExtractor(listOf("src", "dest"))
        assertEquals(false, extractor.remoteSrcOrDest)
    }

    @Test
    fun remoteSrc() {
        val extractor =
            RsyncArgExtractor(listOf("user@example.com:src", "dest"))
        assertEquals(true, extractor.remoteSrcOrDest)
    }

    @Test
    fun remoteDest() {
        val extractor =
            RsyncArgExtractor(listOf("src", "user@example.com:dest"))
        assertEquals(true, extractor.remoteSrcOrDest)
    }

    @Test
    fun remoteNoUser() {
        val extractor = RsyncArgExtractor(listOf("src", "example.com:dest"))
        assertEquals(true, extractor.remoteSrcOrDest)
    }

    @Test
    fun remoteHomeDir() {
        val extractor = RsyncArgExtractor(listOf("src", "user@example.com:"))
        assertEquals(true, extractor.remoteSrcOrDest)
    }
}
