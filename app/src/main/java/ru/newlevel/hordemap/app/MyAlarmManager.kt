package ru.newlevel.hordemap.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import java.util.Date

class MyAlarmManager(private val context: Context) {
    private val intent = Intent(context, MyAlarmReceiver::class.java)
    private val pendingIntent: PendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    fun startAlarmManager() {
        Log.e("AAA", "startAlarmManager at " + Date(System.currentTimeMillis()))
        pendingIntent.let {
            (context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.setAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 240000, it
            )
        }
    }
    fun stopAlarmManager() {
        Log.e("AAA", "stopAlarmManager at " + Date(System.currentTimeMillis()))
        pendingIntent.let {
            (context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.cancel(it)
        }
    }
}