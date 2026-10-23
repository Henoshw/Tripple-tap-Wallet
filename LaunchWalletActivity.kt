package no.henos.trippelwallet

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast

/**
 * Usynlig mellomledd. Trippeltrykk mens skjermen er på ender med at
 * skjermen er AV, så denne aktiviteten slår skjermen på, ber om
 * fingeravtrykk/PIN og åpner deretter Google Wallet.
 */
class LaunchWalletActivity : Activity() {

    companion object {
        const val WALLET_PACKAGE = "com.google.android.apps.walletnfcrel"

        fun openWallet(context: Context): Boolean {
            val launch = context.packageManager.getLaunchIntentForPackage(WALLET_PACKAGE)
            return if (launch != null) {
                context.startActivity(launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                true
            } else {
                Toast.makeText(context, "Google Wallet er ikke installert", Toast.LENGTH_LONG).show()
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$WALLET_PACKAGE"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        val km = getSystemService(KeyguardManager::class.java)
        if (km.isKeyguardLocked) {
            km.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() { launchAndFinish() }
                override fun onDismissCancelled() { finish() }
                override fun onDismissError() { launchAndFinish() }
            })
        } else {
            launchAndFinish()
        }
    }

    private fun launchAndFinish() {
        openWallet(this)
        finish()
    }
}
