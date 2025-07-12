/*
 * Copyright © 2022-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.os.Parcelable
import android.util.AtomicFile
import androidx.core.util.readText
import androidx.core.util.writeText
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.Date

@Parcelize
@Serializable
data class VersionInfo(
    @Serializable(with = VersionSerializer::class)
    val version: Version,
    @Serializable(with = URLSerializer::class)
    val download: URL,
    @Serializable(with = URLSerializer::class)
    val info: URL,
) : Parcelable {
    private object VersionSerializer : KSerializer<Version> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Version", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Version {
            try {
                return Version(decoder.decodeString())
            } catch (exp: IllegalArgumentException) {
                throw SerializationException(exp)
            }
        }

        override fun serialize(
            encoder: Encoder,
            value: Version,
        ) {
            encoder.encodeString(value.toString())
        }
    }

    private object URLSerializer : KSerializer<URL> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("URL", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): URL {
            try {
                return URL(decoder.decodeString())
            } catch (exp: MalformedURLException) {
                throw SerializationException(exp)
            }
        }

        override fun serialize(
            encoder: Encoder,
            value: URL,
        ) {
            encoder.encodeString(value.toString())
        }
    }

    companion object {
        private const val CACHE_INFO_FOR = 1000 * 60 * 60 * 24 * 7

        private val ignoreUnknownJson: Json by lazy {
            Json { ignoreUnknownKeys = true }
        }

        fun fetch(
            httpConn: HttpURLConnection,
            cacheFile: AtomicFile,
        ): VersionInfo {
            var info: VersionInfo? = null
            val cacheModified = cacheFile.baseFile.lastModified()

            if (cacheModified > 0 &&
                cacheModified + CACHE_INFO_FOR > Date().time
            ) {
                val data = cacheFile.readText()

                try {
                    info = Json.decodeFromString<VersionInfo>(data)
                } catch (exp: SerializationException) {
                    // Can't be deserialised so fetch fresh data
                }
            }

            if (info == null) {
                val data =
                    httpConn.inputStream
                        .bufferedReader()
                        .use(BufferedReader::readText)

                info = ignoreUnknownJson.decodeFromString<VersionInfo>(data)
                cacheFile.writeText(data)
            }

            return info
        }
    }
}
