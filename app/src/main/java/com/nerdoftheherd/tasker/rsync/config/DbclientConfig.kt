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
class DbclientConfig @JvmOverloads constructor(
    @field:TaskerInputField("args", R.string.arguments)
    var args: String? = "user@example.com true",
    var checkForUpdates: Boolean = true
)
