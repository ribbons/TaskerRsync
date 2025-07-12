/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.helpers

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.nerdoftheherd.tasker.rsync.RsyncRunner
import com.nerdoftheherd.tasker.rsync.config.RsyncConfig
import com.nerdoftheherd.tasker.rsync.output.CommandOutput

class RsyncHelper(
    config: TaskerPluginConfig<RsyncConfig>,
) : TaskerPluginConfigHelper<RsyncConfig, CommandOutput, RsyncRunner>(config) {
    override val runnerClass = RsyncRunner::class.java
    override val inputClass = RsyncConfig::class.java
    override val outputClass = CommandOutput::class.java
}
