/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.helpers

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelperNoInput
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigNoInput
import com.nerdoftheherd.tasker.rsync.PublicKeyRunner
import com.nerdoftheherd.tasker.rsync.output.PublicKeyOutput

class PublicKeyHelper(
    config: TaskerPluginConfigNoInput,
) : TaskerPluginConfigHelperNoInput<PublicKeyOutput, PublicKeyRunner>(config) {
    override val runnerClass = PublicKeyRunner::class.java
    override val outputClass = PublicKeyOutput::class.java
}
