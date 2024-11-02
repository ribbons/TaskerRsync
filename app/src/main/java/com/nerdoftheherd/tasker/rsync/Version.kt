/*
 * Copyright © 2022-2024 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Objects

@Parcelize
class Version(private val value: String) : Parcelable {
    @IgnoredOnParcel
    private val numParts: List<Int>

    @IgnoredOnParcel
    private val suffixRevs: Int

    init {
        val matches =
            verPattern.matchEntire(value)
                ?: throw IllegalArgumentException("Unexpected version format \"$value\"")

        numParts = matches.groupValues[1].split('.').map { it.toInt() }
        suffixRevs = matches.groups[2]?.value?.toInt() ?: 0
    }

    operator fun compareTo(other: Version): Int {
        for (i in 0 until maxOf(numParts.size, other.numParts.size)) {
            val thisPartNum = numParts.getOrElse(i) { 0 }
            val otherPartNum = other.numParts.getOrElse(i) { 0 }

            when {
                thisPartNum > otherPartNum -> return 1
                thisPartNum < otherPartNum -> return -1
            }
        }

        return when {
            suffixRevs > other.suffixRevs -> 1
            suffixRevs < other.suffixRevs -> -1
            else -> 0
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Version) {
            return false
        }

        return compareTo(other) == 0
    }

    override fun hashCode() = Objects.hash(value)

    override fun toString(): String {
        return value
    }

    companion object {
        private val verPattern = Regex("([0-9]+(?:[.][0-9]+)*)(?:-([0-9]+)-[a-f0-9]+)?")

        val current: Version by lazy {
            Version(BuildConfig.VERSION_NAME)
        }
    }
}
