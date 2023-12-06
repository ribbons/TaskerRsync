/*
 * Copyright © 2021-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.config

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
class DbclientConfig
    @JvmOverloads
    constructor(
        @field:TaskerInputField("args", labelResIdName = "arguments")
        var args: String? = "user@example.com true",
        @field:TaskerInputField("knownHosts", labelResIdName = "known_hosts")
        var knownHosts: String? = "example.com ssh-rsa ABCD1234...=",
        @field:TaskerInputField("timeoutSeconds", labelResIdName = "timeout_seconds")
        var timeoutSeconds: Int = 59,
        @field:TaskerInputField("checkForUpdates", labelResIdName = "check_for_updates")
        var checkForUpdates: Boolean = true,
    )
