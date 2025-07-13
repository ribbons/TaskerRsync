/*
 * Copyright © 2022-2025 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync.activities

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nerdoftheherd.tasker.rsync.R
import com.nerdoftheherd.tasker.rsync.Version
import com.nerdoftheherd.tasker.rsync.VersionInfo
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL

@RunWith(AndroidJUnit4::class)
class UpdateActivityTest {
    @get:Rule
    val intentsRule = IntentsRule()

    @Test
    fun newerVersion() {
        val versionInfo =
            VersionInfo(
                Version("9999"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val intent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                UpdateActivity::class.java,
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TASK

        intent.putExtra("info", versionInfo)

        launchActivity<UpdateActivity>(intent).use {
            onView(withId(R.id.toolbar)).check(
                matches(hasDescendant(withText(R.string.update_available))),
            )
            onView(withId(R.id.buttonUpdate)).check(matches(isEnabled()))
            onView(withId(R.id.textInfo)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun sameVersion() {
        val versionInfo =
            VersionInfo(
                Version.current,
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val intent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                UpdateActivity::class.java,
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TASK

        intent.putExtra("info", versionInfo)

        launchActivity<UpdateActivity>(intent).use {
            onView(withId(R.id.toolbar)).check(
                matches(hasDescendant(withText(R.string.update_installed))),
            )
            onView(withId(R.id.textSummary)).check(
                matches(withText(R.string.updated_summary)),
            )
            onView(withId(R.id.buttonUpdate)).check(matches(not(isEnabled())))
            onView(withId(R.id.textInfo)).check(matches(not(isDisplayed())))
        }
    }

    @Test
    fun moreInfoViewsUrl() {
        val versionInfo =
            VersionInfo(
                Version("9999"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val intent =
            Intent(
                ApplicationProvider.getApplicationContext(),
                UpdateActivity::class.java,
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_CLEAR_TASK

        intent.putExtra("info", versionInfo)

        launchActivity<UpdateActivity>(intent).use {
            intending(hasAction(Intent.ACTION_VIEW))
                .respondWith(
                    Instrumentation.ActivityResult(Activity.RESULT_OK, null),
                )

            onView(withId(R.id.buttonMoreInfo)).perform(click())

            intended(
                allOf(
                    hasAction(Intent.ACTION_VIEW),
                    hasData(versionInfo.info.toString()),
                ),
            )
        }
    }
}
