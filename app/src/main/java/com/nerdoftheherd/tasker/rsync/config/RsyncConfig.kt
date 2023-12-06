/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.config

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
class RsyncConfig
    @JvmOverloads
    constructor(
        @field:TaskerInputField("args", labelResIdName = "arguments")
        var args: String? = "-rv /source/ user@example.com:dest/",
        @field:TaskerInputField("knownHosts", labelResIdName = "known_hosts")
        var knownHosts: String? = "example.com ssh-rsa ABCD1234...=",
        @field:TaskerInputField("checkForUpdates", labelResIdName = "check_for_updates")
        var checkForUpdates: Boolean = true,
    )
