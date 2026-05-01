package edu.cit.gako.brainbox.features.playback.audio

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import edu.cit.gako.brainbox.MainActivity
import edu.cit.gako.brainbox.R
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioSnapshot

internal class BrainBoxAudioNotification(private val context: Context) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        ensureChannel()
    }

    fun startForeground(service: BrainBoxAudioService, snapshot: BrainBoxAudioSnapshot) {
        ServiceCompat.startForeground(
            service,
            NOTIFICATION_ID,
            buildNotification(snapshot),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )
    }

    fun update(snapshot: BrainBoxAudioSnapshot) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(snapshot))
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun buildNotification(snapshot: BrainBoxAudioSnapshot): android.app.Notification {
        val request = snapshot.request
        val contentTitle = request?.notebookTitle?.ifBlank { "BrainBox audio" } ?: "BrainBox audio"
        val contentText = when {
            !snapshot.errorMessage.isNullOrBlank() -> snapshot.errorMessage
            snapshot.status == BrainBoxAudioPlaybackStatus.PLAYING -> "Playing audio"
            snapshot.status == BrainBoxAudioPlaybackStatus.PAUSED -> "Paused"
            snapshot.status == BrainBoxAudioPlaybackStatus.READY -> "Ready to resume"
            snapshot.status == BrainBoxAudioPlaybackStatus.LOADING -> "Preparing audio..."
            snapshot.status == BrainBoxAudioPlaybackStatus.ENDED -> "Playback finished"
            else -> "Preparing audio..."
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(buildContentIntent())
            .setOnlyAlertOnce(true)
            .setOngoing(snapshot.hasLoadedRequest)
            .setSilent(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun buildContentIntent(): PendingIntent {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) {
            return
        }

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "BrainBox audio",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Playback controls for BrainBox audio sessions."
                setShowBadge(false)
            }
        )
    }

    private companion object {
        const val CHANNEL_ID = "brainbox_audio_playback"
        const val NOTIFICATION_ID = 4102
    }
}
