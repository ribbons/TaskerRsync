/*
 * Copyright © 2022-2023 Matt Robinson
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nerdoftheherd.tasker.rsync

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.AtomicFile
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nerdoftheherd.tasker.rsync.activities.UpdateActivity
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class UpdateNotifier {
    companion object {
        private const val VERSION_URL = "https://nerdoftheherd.com/projects/rsync-for-tasker/version.json"
        private const val NOTIF_CHANNEL = "updates"
        private const val NOTIF_ID = 0

        fun checkInBackground(context: Context) {
            thread { check(context) }
        }

        @Synchronized
        private fun check(context: Context) {
            val current = Version.current(context)

            val remoteURL = URL("$VERSION_URL?v=$current")
            val httpConn = remoteURL.openConnection() as HttpURLConnection
            val cacheFile = File(context.filesDir, "version")

            val info = VersionInfo.fetch(httpConn, AtomicFile(cacheFile))

            if (info.version > current) {
                Log.d(TAG, "Update found, about to show notification")
                createNotificationChannel(context)
                showNotification(context, info)
            }
        }

        private fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return
            }

            val channel =
                NotificationChannel(
                    NOTIF_CHANNEL,
                    context.getString(R.string.channel_updates),
                    NotificationManager.IMPORTANCE_LOW,
                )

            val notificationManager =
                context.getSystemService(
                    Context.NOTIFICATION_SERVICE,
                ) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }

        private fun showNotification(
            context: Context,
            info: VersionInfo,
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return
            }

            val updateIntent = Intent(context, UpdateActivity::class.java)
            updateIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            updateIntent.putExtra("info", info)

            val pendingFlags =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

            val updatePI = PendingIntent.getActivity(context, 0, updateIntent, pendingFlags)

            val notifBuilder =
                NotificationCompat.Builder(context, NOTIF_CHANNEL)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(context.getString(R.string.update_available))
                    .setContentText(context.getString(R.string.update_notif_text, info.version))
                    .setContentIntent(updatePI)
                    .setColor(ContextCompat.getColor(context, R.color.primary))

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                notifBuilder.priority = NotificationCompat.PRIORITY_LOW
            }

            NotificationManagerCompat.from(context)
                .notify(NOTIF_ID, notifBuilder.build())
        }
    }
}
