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
            var escape = false
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
                    if (escape) {
                        escape = false

                        when (c) {
                            '\n' -> {
                                return@forEach
                            }
                            '"', '\\' -> {
                                arg += c
                                return@forEach
                            }
                            else -> {
                                arg += '\\'
                            }
                        }
                    }

                    when (c) {
                        '"' -> {
                            doubleQuotes = false
                        }
                        '\\' -> {
                            escape = true
                        }
                        else -> {
                            arg += c
                        }
                    }
                } else {
                    if (escape) {
                        if (c != '\n') {
                            arg += c
                        }

                        escape = false
                        return@forEach
                    }

                    when (c) {
                        '\\' -> {
                            escape = true
                        }
                        '\'' -> {
                            singleQuotes = true
                        }
                        '"' -> {
                            doubleQuotes = true
                        }
                        ' ', '\t', '\n' -> {
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
