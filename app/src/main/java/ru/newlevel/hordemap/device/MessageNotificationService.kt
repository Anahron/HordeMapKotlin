package ru.newlevel.hordemap.device

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.ACTION_OPEN_MESSENGER
import ru.newlevel.hordemap.app.CHANEL_MESSAGE
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.presentation.MainActivity

class MessageNotificationService(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    fun showNotification(number: Int) {
        try {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.action = ACTION_OPEN_MESSENGER
            val pendingIntent = PendingIntent.getActivity(
                    context,
                    9992,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

            val notification = NotificationCompat.Builder(context, CHANEL_MESSAGE)
                .setSmallIcon(R.mipmap.hordecircle_round)
                .setContentTitle("Horde Chat ($number)")
                .setContentText("Есть непрочитанное сообщение(я)")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setNumber(number)
                .setAutoCancel(true)

            notificationManager.notify(2, notification.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification", e)
        }
    }

    fun hideNotification(){
        notificationManager.cancel(2)
    }
}