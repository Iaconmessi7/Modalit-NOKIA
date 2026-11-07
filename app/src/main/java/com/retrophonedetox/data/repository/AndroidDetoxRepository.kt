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
import com.retrophonedetox.ui.theme.NokiaLcdPalette

/**
 * Implementazione concreta del Repository Android per US 1.3 e US 1.4:
 * - Salva in locale (SharedPreferences) la Whitelist delle app scelte e il tema Anti-Luce Blu.
 * - Apre le vere app installate sul telefono tramite Intent di sistema (stile Super Risparmio Energetico).
 * - Apre i pannelli rapidi di Wi-Fi, Rete, Bluetooth e Hotspot senza passare dal menu generale Impostazioni.
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
        enabledApps.forEach { allowed.add(it.packageName) }

        // Aggiungiamo anche il pacchetto SMS predefinito effettivo del dispositivo
        Telephony.Sms.getDefaultSmsPackage(context)?.let { allowed.add(it) }
        return allowed
    }

    override fun launchRealApp(appId: String, packageName: String, title: String): AppLaunchResult {
        return try {
            val intent: Intent? = when (appId) {
                // 1. Telefono: apre sempre il compositore telefonico reale del dispositivo
                "phone" -> Intent(Intent.ACTION_DIAL)

                // 2. Messaggi SMS: apre l'app SMS predefinita del telefono
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

                // 3. Sveglia / Orologio: prova il pacchetto o l'azione di sistema sveglia
                "clock" -> {
                    context.packageManager.getLaunchIntentForPackage(packageName)
                        ?: Intent(AlarmClock.ACTION_SHOW_ALARMS)
                }

                // 4. Calcolatrice: prova il pacchetto o la categoria calcolatrice standard
                "calculator" -> {
                    context.packageManager.getLaunchIntentForPackage(packageName)
                        ?: Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_CALCULATOR)
                        }
                }

                // 5. Fotocamera: apre l'app fotocamera reale del telefono
                "camera" -> {
                    context.packageManager.getLaunchIntentForPackage(packageName)
                        ?: Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                }

                // 6. Qualsiasi altra app (WhatsApp, Google Maps, Spotify, ecc.): cerca il pacchetto installato
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
     * US 1.4 - Apre i pannelli rapidi di sistema (Android Settings Panel / Intent mirati)
     * per Wi-Fi, Dati Mobili, Bluetooth e Hotspot senza aprire la home delle Impostazioni.
     */
    override fun openConnectionControl(option: ConnectionOption): Boolean {
        return try {
            val intent = when (option) {
                ConnectionOption.WIFI_PANEL -> {
                    // Android 10+ (API 29+): apre il pannello fluttuante del Wi-Fi sopra il Nokia!
                    Intent(Settings.Panel.ACTION_WIFI)
                }

                ConnectionOption.INTERNET_DATA -> {
                    // Pannello fluttuante Connessioni Internet (Wi-Fi + Dati Mobili + Modalità Aereo)
                    Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
                }

                ConnectionOption.BLUETOOTH -> {
                    Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                }

                ConnectionOption.HOTSPOT -> {
                    // Tenta l'apertura diretta della schermata Tethering/Hotspot, altrimenti pannello Wireless
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
                // Fallback sicuro se il produttore (Samsung/Xiaomi) usa un nome diverso per l'Hotspot
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
    }
}
