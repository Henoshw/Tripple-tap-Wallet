package no.henos.trippelwallet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Starter tjenesten automatisk etter omstart av telefonen. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(MainActivity.KEY_ENABLED, false)) {
            context.startForegroundService(Intent(context, PressService::class.java))
        }
    }
}
