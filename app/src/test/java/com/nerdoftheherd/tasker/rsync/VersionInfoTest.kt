/*
 * Copyright © 2022-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.io.File
import java.net.URL

class VersionInfoTest {
    @Test
    fun decodeSampleJson() {
        val info =
            VersionInfo(
                Version("1.2.3"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val json = """{
            "version": "1.2.3",
            "download": "http://example.com/",
            "info": "http://example.org/"
        }"""

        val result = Json.decodeFromString<VersionInfo>(json)

        assertEquals(info, result)
    }

    @Test
    fun encodeDecodeJson() {
        val info =
            VersionInfo(
                Version("1.2.3"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val json = Json.encodeToString(info)
        val result = Json.decodeFromString<VersionInfo>(json)

        assertEquals(info, result)
    }

    @Test
    fun fetchRemoteInfo() {
        val info =
            VersionInfo(
                Version("1.2.3"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val cache = File.createTempFile(::fetchRemoteInfo.name, null)
        cache.delete()

        val fetched =
            VersionInfo.fetch(
                MockHttpURLConnection(Json.encodeToString(info)),
                FakeAtomicFile(cache),
            )

        assertEquals(info, fetched)
    }

    @Test
    fun fetchRemoteInfoIgnoreExtraFields() {
        val json =
            """{
                "version": "1.2.3",
                "download": "http://example.com/",
                "info": "http://example.org/",
                "extra": "xyz",
                "extra1": 123
            }"""

        val info =
            VersionInfo(
                Version("1.2.3"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val cache = File.createTempFile(::fetchRemoteInfo.name, null)
        cache.delete()

        val fetched =
            VersionInfo.fetch(
                MockHttpURLConnection(json),
                FakeAtomicFile(cache),
            )

        assertEquals(info, fetched)
    }

    @Test
    fun fetchCachedInfo() {
        val info =
            VersionInfo(
                Version("1.2.3"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val cache = File.createTempFile(::fetchCachedInfo.name, null)
        cache.delete()

        VersionInfo.fetch(
            MockHttpURLConnection(Json.encodeToString(info)),
            FakeAtomicFile(cache),
        )

        val badConn = MockHttpURLConnection(null)
        val fetched = VersionInfo.fetch(badConn, FakeAtomicFile(cache))

        assertEquals(info, fetched)
        assertFalse(badConn.requestWasMade)
        cache.delete()
    }

    @Test
    fun fetchIgnoreStaleCache() {
        val cachedInfo =
            VersionInfo(
                Version("1.2.3"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val cache = File.createTempFile(::fetchIgnoreStaleCache.name, null)
        cache.writeText(Json.encodeToString(cachedInfo))
        cache.setLastModified(1652572800000)

        val remoteInfo =
            VersionInfo(
                Version("1.2.4"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val conn = MockHttpURLConnection(Json.encodeToString(remoteInfo))
        val fetched = VersionInfo.fetch(conn, FakeAtomicFile(cache))

        assertEquals(remoteInfo, fetched)
        cache.delete()
    }

    @Test
    fun fetchIgnoreInvalidCachedJson() {
        val cachedData =
            """
            {
                "version": "1.2.3",
                "download
            """.trimIndent()

        val cache = File.createTempFile(::fetchIgnoreInvalidCachedJson.name, null)
        cache.writeText(cachedData)

        val remoteInfo =
            VersionInfo(
                Version("1.2.4"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val conn = MockHttpURLConnection(Json.encodeToString(remoteInfo))
        val fetched = VersionInfo.fetch(conn, FakeAtomicFile(cache))

        assertEquals(remoteInfo, fetched)
        cache.delete()
    }

    @Test
    fun fetchIgnoreInvalidCachedVersion() {
        val cachedData =
            """
            {
                "version": "invalid",
                "download": "http://example.com/",
                "info": "http://example.org/"
            }
            """.trimIndent()

        val cache = File.createTempFile(::fetchIgnoreInvalidCachedVersion.name, null)
        cache.writeText(cachedData)

        val remoteInfo =
            VersionInfo(
                Version("1.2.3"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val conn = MockHttpURLConnection(Json.encodeToString(remoteInfo))
        val fetched = VersionInfo.fetch(conn, FakeAtomicFile(cache))

        assertEquals(remoteInfo, fetched)
        cache.delete()
    }

    @Test
    fun fetchIgnoreInvalidCachedURL() {
        val cachedData =
            """
            {
                "version": "1.2.3",
                "download": "invalid",
                "info": "invalid"
            }
            """.trimIndent()

        val cache = File.createTempFile(::fetchIgnoreInvalidCachedURL.name, null)
        cache.writeText(cachedData)

        val remoteInfo =
            VersionInfo(
                Version("1.2.4"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val conn = MockHttpURLConnection(Json.encodeToString(remoteInfo))
        val fetched = VersionInfo.fetch(conn, FakeAtomicFile(cache))

        assertEquals(remoteInfo, fetched)
        cache.delete()
    }

    @Test
    fun fetchIgnoreInvalidExtraFields() {
        val cachedData =
            """
            {
                "version": "1.2.3",
                "download": "http://example.com/",
                "info": "http://example.org/",
                "extra": "xyz",
                "extra1": 123
            }
            """.trimIndent()

        val cache = File.createTempFile(::fetchIgnoreInvalidCachedURL.name, null)
        cache.writeText(cachedData)

        val remoteInfo =
            VersionInfo(
                Version("1.2.4"),
                URL("http://example.com/"),
                URL("http://example.org/"),
            )

        val conn = MockHttpURLConnection(Json.encodeToString(remoteInfo))
        val fetched = VersionInfo.fetch(conn, FakeAtomicFile(cache))

        assertEquals(remoteInfo, fetched)
        cache.delete()
    }
}
