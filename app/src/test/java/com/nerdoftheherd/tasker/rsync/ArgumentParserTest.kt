/*
 * Copyright © 2022 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ArgumentParserTest {
    @Test
    fun parseSimple() {
        val args = ArgumentParser.parse("one two three")
        assertEquals(arrayListOf("one", "two", "three"), args)
    }

    @Test
    fun parseExtraSpaces() {
        val args = ArgumentParser.parse("  one   two   three  ")
        assertEquals(arrayListOf("one", "two", "three"), args)
    }

    @Test
    fun parseDoubleQuotes() {
        val args = ArgumentParser.parse("\"one two\" three")
        assertEquals(arrayListOf("one two", "three"), args)
    }

    @Test
    fun parseEmbeddedDoubleQuotes() {
        val args = ArgumentParser.parse("one\" two \"three")
        assertEquals(arrayListOf("one two three"), args)
    }
}
