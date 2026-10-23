package no.henos.trippelwallet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.VibratorManager

/**
 * Android sender ingen egen hendelse for "av/på-knappen ble trykket".
 * Hvert trykk slår derimot skjermen av eller på, så vi teller
 * SCREEN_ON/SCREEN_OFF. Tre skifter innen WINDOW_MS = trippeltrykk.
 */
class PressService : Service() {

    companion object {
        const val WINDOW_MS = 1500L       // alle tre trykk må skje innen dette
        const val PRESSES = 3
        const val COOLDOWN_MS = 2500L     // hindrer dobbel utløsning
        private const val CHANNEL_ID = "trippel_wallet"
        private const val NOTIF_ID = 1

        @Volatile var running = false
            private set
    }

    private val presses = ArrayDeque<Long>()
    private var lastTrigger = 0L

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != Intent.ACTION_SCREEN_ON &&
                intent.action != Intent.ACTION_SCREEN_OFF) return
            onPress()
        }
    }

    private fun onPress() {
        val now = SystemClock.elapsedRealtime()
        if (now - lastTrigger < COOLDOWN_MS) return

        presses.addLast(now)
        while (presses.isNotEmpty() && now - presses.first() > WINDOW_MS) {
            presses.removeFirst()
        }
        if (presses.size >= PRESSES) {
            presses.clear()
            lastTrigger = now
            vibrate()
            startActivity(
                Intent(this, LaunchWalletActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    private fun vibrate() {
        val vm = getSystemService(VibratorManager::class.java) ?: return
        vm.defaultVibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NOTIF_ID, buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
        registerReceiver(screenReceiver, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        })
        running = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        running = false
        unregisterReceiver(screenReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Trippeltrykk aktiv", NotificationManager.IMPORTANCE_MIN)
                .apply { setShowBadge(false) }
        )
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app)
            .setContentTitle("Trippel Wallet er aktiv")
            .setContentText("Trykk av/på-knappen 3 ganger for å åpne Google Wallet")
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
    }
}
