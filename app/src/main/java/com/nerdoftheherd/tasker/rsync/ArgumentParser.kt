/*
 * Copyright © 2022 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

class ArgumentParser {
    companion object {
        fun parse(args: String?): List<String> {
            if (args == null) {
                return ArrayList()
            }

            var arg = ""
            var inQuotes = false
            val parsed = ArrayList<String>()

            args.forEach { c ->
                if (inQuotes) {
                    if (c == '"') {
                        inQuotes = false
                    } else {
                        arg += c
                    }
                } else {
                    when (c) {
                        '"' -> {
                            inQuotes = true
                        }
                        ' ' -> {
                            parsed.add(arg)
                            arg = ""
                        }
                        else -> {
                            arg += c
                        }
                    }
                }
            }

            parsed.add(arg)
            return parsed
        }
    }
}
