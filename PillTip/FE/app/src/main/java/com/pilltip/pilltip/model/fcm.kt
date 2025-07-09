package com.pilltip.pilltip.model

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pilltip.pilltip.MainActivity
import com.pilltip.pilltip.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "새로운 토큰 발급됨: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "알림 수신 성공")

        val title = remoteMessage.data["title"] ?: "PillTip"
        val body = remoteMessage.data["body"] ?: "복약 알림이에요!"

        sendNotification(title, body)
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("notification_title", title)
            putExtra("notification_body", messageBody)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        // 5분 뒤 다시 알림 버튼
        val snoozeIntent = Intent(this, SnoozeReceiver::class.java).apply {
            putExtra("notification_id", 0)
            putExtra("title", title)
            putExtra("body", messageBody)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this, 1, snoozeIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 복약 완료 버튼
        val markAsTakenIntent = Intent(this, MarkAsTakenReceiver::class.java).apply {
            putExtra("notification_id", 0)
        }
        val markAsTakenPendingIntent = PendingIntent.getBroadcast(
            this, 2, markAsTakenIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_pilltip)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .addAction(0, "5분 뒤 다시 알림", snoozePendingIntent)
            .addAction(0, "복약 완료", markAsTakenPendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "기본 알림 채널",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}

class SnoozeReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("notification_id", 0)
        val title = intent.getStringExtra("title") ?: "PillTip"
        val body = intent.getStringExtra("body") ?: "다시 알림을 보내드려요!"

        Log.d("FCM", "5분 뒤 다시 알림 예약됨 (id=$id)")

        val newIntent = Intent(context, MyFirebaseMessagingService::class.java).apply {
            action = "RE_NOTIFY"
            putExtra("title", title)
            putExtra("body", body)
        }

        val pendingIntent = PendingIntent.getService(
            context, 99, newIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = System.currentTimeMillis() + 5 * 60 * 1000 // 5분 후

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

        Toast.makeText(context, "5분 뒤 다시 알림을 설정했어요.", Toast.LENGTH_SHORT).show()
    }
}

class MarkAsTakenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("notification_id", 0)
        Log.d("FCM", "복약 완료 버튼 클릭됨 (id=$id)")

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(id)

        Toast.makeText(context, "복약 완료로 처리했어요!", Toast.LENGTH_SHORT).show()

        // TODO: 서버로 복약 완료 상태 전송 등 처리
    }
}