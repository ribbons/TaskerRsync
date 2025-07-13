/*
 * Copyright © 2025 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.activities

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RsyncConfigActivityTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<RsyncConfigActivity> =
        ActivityScenarioRule(RsyncConfigActivity::class.java)

    @Test
    fun recreate() {
        activityRule.scenario.recreate()
    }
}
