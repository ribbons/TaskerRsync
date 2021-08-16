/*
 * Copyright © 2021 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrivateKeyConfigActivityTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<PrivateKeyConfigActivity> =
        ActivityScenarioRule(PrivateKeyConfigActivity::class.java)

    @Test
    fun defaultKeyType() {
        onView(withId(R.id.key_type)).check(matches(withSpinnerText("Ed25519")))
    }

    @Test
    fun ed25519Keysize() {
        onView(withId(R.id.key_type)).perform(click())
        onView(withText("Ed25519")).perform(click())
        onView(withId(R.id.key_size)).check(matches(withSpinnerText("256")))
    }

    @Test
    fun ecdsaKeysizes() {
        onView(withId(R.id.key_type)).perform(click())
        onView(withText("ECDSA")).perform(click())
        onView(withId(R.id.key_size)).check(matches(withSpinnerText("384")))

        onView(withId(R.id.key_size)).perform(click())
        onView(withText("521")).check(matches(isDisplayed()))
    }

    @Test
    fun rsaKeysizes() {
        onView(withId(R.id.key_type)).perform(click())
        onView(withText("RSA")).perform(click())
        onView(withId(R.id.key_size)).check(matches(withSpinnerText("2048")))

        onView(withId(R.id.key_size)).perform(click())
        onView(withText("4096")).check(matches(isDisplayed()))
    }
}
