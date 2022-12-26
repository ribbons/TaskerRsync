/*
 * Copyright © 2021-2022 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.config

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.nerdoftheherd.tasker.rsync.R

@TaskerInputRoot
class RsyncConfig @JvmOverloads constructor(
    @field:TaskerInputField("args", R.string.arguments)
    var args: String? = "-rv /source/ user@example.com:dest/",
    @field:TaskerInputField("knownHosts", R.string.known_hosts)
    var knownHosts: String? = "example.com ssh-rsa ABCD1234...=",
    @field:TaskerInputField("checkForUpdates", R.string.check_for_updates)
    var checkForUpdates: Boolean = true
)
