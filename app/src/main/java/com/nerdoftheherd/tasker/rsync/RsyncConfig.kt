/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot

@TaskerInputRoot
class RsyncConfig @JvmOverloads constructor(
    @field:TaskerInputField("args", R.string.arguments)
    var args: String? = "-rv /source/ user@example.com:dest/",
)
