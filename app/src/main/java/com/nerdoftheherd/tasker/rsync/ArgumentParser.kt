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

            return args.split(" ")
        }
    }
}
