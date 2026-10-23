package no.henos.trippelwallet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/** Oppsettskjerm: gi tillatelser og slå funksjonen av/på. */
class MainActivity : Activity() {

    companion object {
        const val PREFS = "trippel_wallet"
        const val KEY_ENABLED = "enabled"
    }

    private lateinit var status: TextView
    private lateinit var overlayBtn: Button
    private lateinit var batteryBtn: Button
    private lateinit var notifBtn: Button
    private lateinit var toggleBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
        }
        root.addView(TextView(this).apply {
            text = "Trippel Wallet"
            textSize = 26f
            setTypeface(typeface, Typeface.BOLD)
        })
        root.addView(TextView(this).apply {
            text = "Trykk av/på-knappen 3 ganger raskt for å åpne Google Wallet.\n\nGi tillatelsene under én gang, og trykk deretter Start."
            textSize = 15f
            setPadding(0, pad / 2, 0, pad)
        })

        status = TextView(this).apply { textSize = 16f; setPadding(0, 0, 0, pad) }
        root.addView(status)

        overlayBtn = button("1. Tillat «Vis over andre apper»") {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        batteryBtn = button("2. Slå av batterioptimalisering") {
            startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
        }
        notifBtn = button("3. Tillat varsel") {
            if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        toggleBtn = button("Start") { toggle() }
        val testBtn = button("Test: åpne Google Wallet nå") { LaunchWalletActivity.openWallet(this) }

        listOf(overlayBtn, batteryBtn, notifBtn, toggleBtn, testBtn).forEach { root.addView(it) }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun button(label: String, onClick: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
        setOnClickListener { onClick() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        refresh()
    }

    private fun hasOverlay() = Settings.canDrawOverlays(this)
    private fun hasBattery() = getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(packageName)
    private fun hasNotif() = Build.VERSION.SDK_INT < 33 ||
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun toggle() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val svc = Intent(this, PressService::class.java)
        if (PressService.running) {
            stopService(svc)
            prefs.edit().putBoolean(KEY_ENABLED, false).apply()
        } else {
            startForegroundService(svc)
            prefs.edit().putBoolean(KEY_ENABLED, true).apply()
        }
        toggleBtn.postDelayed({ refresh() }, 300)
    }

    private fun refresh() {
        fun mark(ok: Boolean) = if (ok) "✅" else "❌"
        overlayBtn.isEnabled = !hasOverlay()
        batteryBtn.isEnabled = !hasBattery()
        notifBtn.isEnabled = !hasNotif()
        toggleBtn.isEnabled = hasOverlay()
        toggleBtn.text = if (PressService.running) "Stopp" else "Start"
        status.text = buildString {
            appendLine("${mark(hasOverlay())} Vis over andre apper (påkrevd)")
            appendLine("${mark(hasBattery())} Ingen batterioptimalisering (anbefalt)")
            appendLine("${mark(hasNotif())} Varsel (anbefalt)")
            append("${mark(PressService.running)} Tjenesten kjører")
        }
    }
}
