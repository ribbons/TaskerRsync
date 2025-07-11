/*
 * Copyright © 2022 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VersionTest {
    @Test
    fun throwForInvalidVersion() {
        assertThrows<IllegalArgumentException> { Version("x.y.z") }
        assertThrows<IllegalArgumentException> { Version("2.1-4") }
        assertThrows<IllegalArgumentException> { Version("2.1-4-xyzabcd") }
    }

    @Test
    fun equalTo() {
        Assertions.assertTrue(Version("2") == Version("2.0"))
        Assertions.assertTrue(Version("2") == Version("2.0.0"))
        Assertions.assertTrue(Version("2.0") == Version("2.0.0"))
    }

    @Test
    fun notEqualTo() {
        Assertions.assertTrue(Version("2") != Version("3"))
        Assertions.assertTrue(Version("2") != Version("2.1"))
        Assertions.assertTrue(Version("2.0") != Version("2.1"))
    }

    @Test
    fun newerThanSimple() {
        Assertions.assertTrue(Version("2") > Version("1"))
        Assertions.assertTrue(Version("2.1") > Version("2.0"))
        Assertions.assertTrue(Version("2.1") > Version("2"))
        Assertions.assertTrue(Version("2.1.1") > Version("2"))
        Assertions.assertTrue(Version("2.1.1") > Version("2.1"))
        Assertions.assertTrue(Version("2.10") > Version("2.9"))
    }

    @Test
    fun newerThanCommitSuffix() {
        Assertions.assertTrue(Version("2-5-abcd123") > Version("2"))
        Assertions.assertTrue(Version("2.1") > Version("2-3-abcd123"))
        Assertions.assertTrue(
            Version("2.1-5-123abcd") > Version("2.1-3-abcd123"),
        )
        Assertions.assertTrue(
            Version("2.1-10-321abcd") > Version("2.1-5-123abcd"),
        )
        Assertions.assertTrue(Version("2.1.1") > Version("2.1-10-321abcd"))
    }
}
