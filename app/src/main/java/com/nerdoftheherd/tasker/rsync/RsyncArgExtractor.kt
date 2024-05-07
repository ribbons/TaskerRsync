/*
 * Copyright © 2024 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

class RsyncArgExtractor(args: List<String>) {
    val paths = ArrayList<String>()

    companion object {
        private val pathArgs =
            hashSetOf(
                "--exclude-from",
                "--include-from",
                "--files-from",
                "--log-file",
                "--write-batch",
                "--only-write-batch",
                "--read-batch",
            )
    }

    init {
        args.forEach { arg ->
            if (arg.startsWith("--")) {
                if (arg.contains('=')) {
                    val (longopt, optval) = arg.split('=', limit = 2)

                    if (pathArgs.contains(longopt)) {
                        paths += optval
                    }

                    return@forEach
                }
            }

            if (!arg.startsWith("-")) {
                paths += arg
            }
        }
    }
}
