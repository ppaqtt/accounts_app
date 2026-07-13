package com.jizhangben.app.ui.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import com.jizhangben.app.MainActivity
import com.jizhangben.app.R
import java.util.Calendar

class ReminderManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

    val isEnabledState = mutableStateOf(prefs.getBoolean("reminder_enabled", false))
    val hourState = mutableStateOf(prefs.getInt("reminder_hour", 20))
    val minuteState = mutableStateOf(prefs.getInt("reminder_minute", 0))

    var isEnabled: Boolean
        get() = isEnabledState.value
        set(value) {
            isEnabledState.value = value
            prefs.edit().putBoolean("reminder_enabled", value).apply()
            if (value) {
                scheduleReminder()
            } else {
                cancelReminder()
            }
        }

    var reminderHour: Int
        get() = hourState.value
        set(value) {
            hourState.value = value
            prefs.edit().putInt("reminder_hour", value).apply()
            if (isEnabled) scheduleReminder()
        }

    var reminderMinute: Int
        get() = minuteState.value
        set(value) {
            minuteState.value = value
            prefs.edit().putInt("reminder_minute", value).apply()
            if (isEnabled) scheduleReminder()
        }

    private fun scheduleReminder() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, reminderHour)
                set(Calendar.MINUTE, reminderMinute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: Exception) { }
    }

    private fun cancelReminder() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) { }
    }

    companion object {
        const val CHANNEL_ID = "reminder_channel"
        const val NOTIFICATION_ID = 1001
    }
}

val LocalReminderManager = compositionLocalOf<ReminderManager> {
    error("No ReminderManager provided")
}

@Composable
fun ProvideReminderManager(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val reminderManager = remember { ReminderManager(context) }
    CompositionLocalProvider(LocalReminderManager provides reminderManager) {
        content()
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderManager.CHANNEL_ID,
                "记账提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "每日记账提醒通知"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderManager.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🐱 小萌记账")
            .setContentText("今天的账记了吗？快来记录一下吧~")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(ReminderManager.NOTIFICATION_ID, notification)
    }
}
