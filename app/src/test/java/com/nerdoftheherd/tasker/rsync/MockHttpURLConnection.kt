/*
 * Copyright © 2022-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MockHttpURLConnection(
    private val responseData: String?,
) : HttpURLConnection(URL("http://example.com/")) {
    var requestWasMade = false
        private set

    override fun connect() {
        requestWasMade = true

        if (responseData == null) {
            throw IOException("Failed")
        }
    }

    override fun disconnect() {}

    override fun usingProxy(): Boolean = false

    override fun getInputStream(): InputStream {
        requestWasMade = true

        if (responseData == null) {
            throw IOException("Failed")
        }

        return ByteArrayInputStream(responseData.toByteArray())
    }
}
