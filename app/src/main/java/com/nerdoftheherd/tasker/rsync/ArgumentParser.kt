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
            var singleQuotes = false
            var doubleQuotes = false
            val parsed = ArrayList<String>()

            args.forEach { c ->
                if (singleQuotes) {
                    if (c == '\'') {
                        singleQuotes = false
                    } else {
                        arg += c
                    }
                } else if (doubleQuotes) {
                    if (c == '"') {
                        doubleQuotes = false
                    } else {
                        arg += c
                    }
                } else {
                    when (c) {
                        '\'' -> {
                            singleQuotes = true
                        }
                        '"' -> {
                            doubleQuotes = true
                        }
                        ' ' -> {
                            if (arg.isNotEmpty()) {
                                parsed.add(arg)
                                arg = ""
                            }
                        }
                        else -> {
                            arg += c
                        }
                    }
                }
            }

            if (arg.isNotEmpty()) {
                parsed.add(arg)
            }

            return parsed
        }
    }
}
