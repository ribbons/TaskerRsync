/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper

class RsyncHelper(config: TaskerPluginConfig<RsyncConfig>) :
    TaskerPluginConfigHelper<RsyncConfig, RsyncOutput, RsyncRunner>(config) {
    override val runnerClass: Class<RsyncRunner> get() = RsyncRunner::class.java
    override val inputClass = RsyncConfig::class.java
    override val outputClass = RsyncOutput::class.java
}
