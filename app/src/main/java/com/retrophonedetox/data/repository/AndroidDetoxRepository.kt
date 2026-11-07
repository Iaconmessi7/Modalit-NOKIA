package com.retrophonedetox.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Telephony
import com.retrophonedetox.domain.model.AllowedApp
import com.retrophonedetox.domain.model.ConnectionOption
import com.retrophonedetox.domain.repository.AppLaunchResult
import com.retrophonedetox.domain.repository.DetoxRepository
import com.retrophonedetox.service.DetoxGuardService
import com.retrophonedetox.ui.theme.NokiaLcdPalette

/**
 * Implementazione concreta del Repository Android per Sprint 1 (US 1.3, 1.4) e Sprint 2 (US 2.1, 2.2).
 */
class AndroidDetoxRepository(
    private val context: Context
) : DetoxRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Catalogo predefinito di app utili selezionabili nella Whitelist del Nokia */
    private val defaultCatalog = listOf(
        AllowedApp(
            id = "phone",
            title = "Telefono",
            packageName = "com.android.dialer",
            description = "Apri l'app Telefono per chiamare",
            isEssential = true,
            isEnabled = true
        ),
        AllowedApp(
            id = "messages",
            title = "Messaggi SMS",
            packageName = "com.google.android.apps.messaging",
            description = "Apri l'app Messaggi SMS del telefono",
            isEssential = true,
            isEnabled = true
        ),
        AllowedApp(
            id = "whatsapp",
            title = "WhatsApp",
            packageName = "com.whatsapp",
            description = "Apri WhatsApp (Messaggistica)",
            isEssential = false,
            isEnabled = true
        ),
        AllowedApp(
            id = "maps",
            title = "Google Maps",
            packageName = "com.google.android.apps.maps",
            description = "Navigatore e mappe stradali",
            isEssential = false,
            isEnabled = false
        ),
        AllowedApp(
            id = "clock",
            title = "Sveglia / Orologio",
            packageName = "com.google.android.deskclock",
            description = "Sveglie, timer e cronometro",
            isEssential = false,
            isEnabled = false
        ),
        AllowedApp(
            id = "calculator",
            title = "Calcolatrice",
            packageName = "com.google.android.calculator",
            description = "Calcolatrice rapida di sistema",
            isEssential = false,
            isEnabled = false
        ),
        AllowedApp(
            id = "camera",
            title = "Fotocamera",
            packageName = "com.android.camera",
            description = "Scatta foto o leggi QR Code",
            isEssential = false,
            isEnabled = false
        ),
        AllowedApp(
            id = "spotify",
            title = "Spotify / Musica",
            packageName = "com.spotify.music",
            description = "Ascolta musica e podcast",
            isEssential = false,
            isEnabled = false
        )
    )

    override fun getConfigurableApps(): List<AllowedApp> {
        val savedIds = prefs.getStringSet(KEY_ENABLED_APP_IDS, null)
        return if (savedIds == null) {
            defaultCatalog
        } else {
            defaultCatalog.map { app ->
                app.copy(isEnabled = app.isEssential || savedIds.contains(app.id))
            }
        }
    }

    override fun toggleAppWhitelist(appId: String): List<AllowedApp> {
        val currentApps = getConfigurableApps()
        val updatedApps = currentApps.map { app ->
            if (app.id == appId && !app.isEssential) {
                app.copy(isEnabled = !app.isEnabled)
            } else {
                app
            }
        }

        val enabledIds = updatedApps.filter { it.isEnabled }.map { it.id }.toSet()
        prefs.edit().putStringSet(KEY_ENABLED_APP_IDS, enabledIds).apply()
        return updatedApps
    }

    override fun getWhitelistedPackages(): Set<String> {
        val allowed = mutableSetOf(context.packageName)
        val enabledApps = getConfigurableApps().filter { it.isEnabled }
        enabledApps.forEach { app ->
            allowed.add(app.packageName)
            // Aggiunge varianti comuni dei produttori (Samsung, Xiaomi, Google) per le app abilitate
            when (app.id) {
                "phone" -> {
                    allowed.add("com.google.android.dialer")
                    allowed.add("com.samsung.android.dialer")
                    allowed.add("com.android.contacts")
                }
                "messages" -> {
                    allowed.add("com.samsung.android.messaging")
                    allowed.add("com.android.mms")
                    Telephony.Sms.getDefaultSmsPackage(context)?.let { allowed.add(it) }
                }
                "clock" -> {
                    allowed.add("com.sec.android.app.clockpackage")
                    allowed.add("com.android.deskclock")
                }
                "calculator" -> {
                    allowed.add("com.sec.android.app.popupcalculator")
                    allowed.add("com.miui.calculator")
                }
                "camera" -> {
                    allowed.add("com.sec.android.app.camera")
                    allowed.add("com.google.android.GoogleCamera")
                }
            }
        }
        return allowed
    }

    override fun launchRealApp(appId: String, packageName: String, title: String): AppLaunchResult {
        return try {
            val intent: Intent? = when (appId) {
                "phone" -> Intent(Intent.ACTION_DIAL)

                "messages" -> {
                    val defaultSmsPkg = Telephony.Sms.getDefaultSmsPackage(context)
                    if (defaultSmsPkg != null) {
                        context.packageManager.getLaunchIntentForPackage(defaultSmsPkg)
                    } else {
                        Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_MESSAGING)
                        }
                    } ?: Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
                }

                "clock" -> {
                    context.packageManager.getLaunchIntentForPackage(packageName)
                        ?: Intent(AlarmClock.ACTION_SHOW_ALARMS)
                }

                "calculator" -> {
                    context.packageManager.getLaunchIntentForPackage(packageName)
                        ?: Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_CALCULATOR)
                        }
                }

                "camera" -> {
                    context.packageManager.getLaunchIntentForPackage(packageName)
                        ?: Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                }

                else -> context.packageManager.getLaunchIntentForPackage(packageName)
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                AppLaunchResult.Launched(title)
            } else {
                AppLaunchResult.NotInstalled(title)
            }
        } catch (e: Exception) {
            AppLaunchResult.NotInstalled(title)
        }
    }

    /**
     * US 1.4 & US 2.2 - Apre i pannelli rapidi di sistema (Wi-Fi, Dati, Bluetooth, Hotspot)
     * e registra una finestra temporanea di 45 secondi (`KEY_SETTINGS_GRACE_UNTIL_MS`)
     * così il "Portiere" lascia usare lo switch di connessione senza bloccarlo.
     */
    override fun openConnectionControl(option: ConnectionOption): Boolean {
        return try {
            // Autorizza temporaneamente il pannello Connessioni per 45 secondi nel Portiere (US 2.2)
            allowConnectionSettingsTemporarily(durationMs = 45_000L)

            val intent = when (option) {
                ConnectionOption.WIFI_PANEL -> Intent(Settings.Panel.ACTION_WIFI)
                ConnectionOption.INTERNET_DATA -> Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                ConnectionOption.BLUETOOTH -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                ConnectionOption.HOTSPOT -> {
                    Intent(Intent.ACTION_MAIN).apply {
                        component = ComponentName(
                            "com.android.settings",
                            "com.android.settings.TetherSettings"
                        )
                    }
                }
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (fallback: Exception) {
                val fallbackIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun isDetoxBlockingActive(): Boolean {
        return prefs.getBoolean(KEY_DETOX_ACTIVE, true)
    }

    override fun setDetoxBlockingActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_DETOX_ACTIVE, active).apply()
    }

    override fun isGuardServiceEnabled(): Boolean {
        return try {
            val expectedComponent = ComponentName(context, DetoxGuardService::class.java).flattenToString()
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ).orEmpty()
            enabledServices.split(':').any { it.equals(expectedComponent, ignoreCase = true) }
        } catch (e: Exception) {
            false
        }
    }

    override fun openAccessibilityPermissionSettings(): Boolean {
        return try {
            // Concede 60 secondi per abilitare il permesso del Portiere nelle Impostazioni di Accessibilità
            allowConnectionSettingsTemporarily(durationMs = 60_000L)
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun allowConnectionSettingsTemporarily(durationMs: Long) {
        val until = System.currentTimeMillis() + durationMs
        prefs.edit().putLong(KEY_SETTINGS_GRACE_UNTIL_MS, until).apply()
    }

    fun isConnectionSettingsTemporarilyAllowed(): Boolean {
        val until = prefs.getLong(KEY_SETTINGS_GRACE_UNTIL_MS, 0L)
        return System.currentTimeMillis() < until
    }

    override fun recordLastBlockedApp(appName: String) {
        val currentCount = getBlockedAttemptsCount()
        prefs.edit()
            .putString(KEY_LAST_BLOCKED_APP, appName)
            .putInt(KEY_BLOCKED_COUNT, currentCount + 1)
            .apply()
    }

    override fun getLastBlockedApp(): String? {
        return prefs.getString(KEY_LAST_BLOCKED_APP, null)
    }

    override fun getBlockedAttemptsCount(): Int {
        return prefs.getInt(KEY_BLOCKED_COUNT, 0)
    }

    fun resolveReadableAppName(packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast('.')
        }
    }

    override fun getBatteryBars(): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val pct = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
            when {
                pct >= 75 -> 4
                pct >= 50 -> 3
                pct >= 25 -> 2
                else -> 1
            }
        } catch (e: Exception) {
            4
        }
    }

    override fun getSavedPalette(): NokiaLcdPalette {
        val savedName = prefs.getString(KEY_PALETTE_NAME, NokiaLcdPalette.SAGE_LCD.name)
        return NokiaLcdPalette.entries.firstOrNull { it.name == savedName }
            ?: NokiaLcdPalette.SAGE_LCD
    }

    override fun savePalette(palette: NokiaLcdPalette) {
        prefs.edit().putString(KEY_PALETTE_NAME, palette.name).apply()
    }

    companion object {
        private const val PREFS_NAME = "modalita_nokia_prefs"
        private const val KEY_ENABLED_APP_IDS = "enabled_whitelist_app_ids"
        private const val KEY_PALETTE_NAME = "selected_lcd_palette"
        private const val KEY_DETOX_ACTIVE = "detox_blocking_active"
        private const val KEY_SETTINGS_GRACE_UNTIL_MS = "settings_grace_until_ms"
        private const val KEY_LAST_BLOCKED_APP = "last_blocked_app_name"
        private const val KEY_BLOCKED_COUNT = "blocked_attempts_count"
    }
}
