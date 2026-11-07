package com.retrophonedetox.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
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
import com.retrophonedetox.domain.model.DetoxSchedule
import com.retrophonedetox.domain.repository.AppLaunchResult
import com.retrophonedetox.domain.repository.DetoxRepository
import com.retrophonedetox.domain.strategy.CustodianCodeStrategy
import com.retrophonedetox.service.DetoxAlarmReceiver
import com.retrophonedetox.service.DetoxGuardService
import com.retrophonedetox.ui.theme.NokiaLcdPalette
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Implementazione concreta del Repository Android per Sprint 1, Sprint 2 e Sprint 3 (US 3.1 & US 3.2).
 */
class AndroidDetoxRepository(
    private val context: Context
) : DetoxRepository {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Catalogo predefinito di app utili selezionabili nella Whitelist del Nokia (US 1.3) */
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

    /**
     * US 3.2 - Fasce orarie ricorrenti predefinite (Stile Sveglia) attivabili/disattivabili.
     */
    private val defaultSchedules = listOf(
        DetoxSchedule(
            id = "morning_focus",
            title = "Mattina 09:00-13:00",
            daysTag = "L-V",
            startHour = 9,
            startMinute = 0,
            endHour = 13,
            endMinute = 0,
            daysOfWeek = setOf(
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY
            ),
            isEnabled = true
        ),
        DetoxSchedule(
            id = "afternoon_study",
            title = "Pomerig. 14:30-18:30",
            daysTag = "L-V",
            startHour = 14,
            startMinute = 30,
            endHour = 18,
            endMinute = 30,
            daysOfWeek = setOf(
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY
            ),
            isEnabled = false
        ),
        DetoxSchedule(
            id = "night_detox",
            title = "Notte 22:30-07:00",
            daysTag = "7gg",
            startHour = 22,
            startMinute = 30,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = setOf(
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY,
                Calendar.SUNDAY
            ),
            isEnabled = true
        ),
        DetoxSchedule(
            id = "weekend_detox",
            title = "Weekend 10:00-14:00",
            daysTag = "S-D",
            startHour = 10,
            startMinute = 0,
            endHour = 14,
            endMinute = 0,
            daysOfWeek = setOf(Calendar.SATURDAY, Calendar.SUNDAY),
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

    override fun openConnectionControl(option: ConnectionOption): Boolean {
        return try {
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
        clearExpiredQuickTimerIfNeeded()
        clearExpiredEmergencyPauseIfNeeded()
        // US 4.1: Se è in corso una Pausa di Emergenza (5 min), il telefono è temporaneamente libero
        if (getRemainingEmergencyPauseMinutes() > 0) return false

        // Il blocco è attivo se:
        // 1. È attivo un timer rapido manuale (US 3.1), oppure
        // 2. Siamo dentro una fascia oraria programmata attiva (US 3.2), oppure
        // 3. Il flag manuale del Portiere è attivo
        if (getRemainingQuickTimerMinutes() > 0) return true
        if (isAnyScheduleCurrentlyActive()) return true
        return prefs.getBoolean(KEY_DETOX_ACTIVE, true)
    }

    override fun setDetoxBlockingActive(active: Boolean) {
        if (!active) {
            // Se viene disattivato manualmente, azzera anche eventuale timer rapido
            prefs.edit()
                .putBoolean(KEY_DETOX_ACTIVE, false)
                .putLong(KEY_QUICK_TIMER_END_MS, 0L)
                .apply()
        } else {
            prefs.edit().putBoolean(KEY_DETOX_ACTIVE, true).apply()
        }
    }

    /**
     * US 3.1 - Avvia subito una sessione Detox manuale per `durationMinutes` minuti
     * e imposta un allarme di sistema (`AlarmManager`) per lo sblocco automatico alla scadenza.
     */
    override fun startQuickDetoxTimer(durationMinutes: Int) {
        val endTimeMs = System.currentTimeMillis() + durationMinutes * 60_000L
        prefs.edit()
            .putLong(KEY_QUICK_TIMER_END_MS, endTimeMs)
            .putBoolean(KEY_DETOX_ACTIVE, true)
            .apply()

        scheduleSystemAlarm(
            triggerAtMillis = endTimeMs,
            action = DetoxAlarmReceiver.ACTION_ALARM_TIMER_EXPIRED,
            requestCode = REQ_CODE_QUICK_TIMER
        )
    }

    override fun getRemainingQuickTimerMinutes(): Int {
        val endTimeMs = prefs.getLong(KEY_QUICK_TIMER_END_MS, 0L)
        val remainingMs = endTimeMs - System.currentTimeMillis()
        return if (remainingMs > 0L) {
            ((remainingMs + 59_999L) / 60_000L).toInt()
        } else {
            0
        }
    }

    override fun clearExpiredQuickTimerIfNeeded() {
        val endTimeMs = prefs.getLong(KEY_QUICK_TIMER_END_MS, 0L)
        if (endTimeMs > 0L && System.currentTimeMillis() >= endTimeMs) {
            prefs.edit().putLong(KEY_QUICK_TIMER_END_MS, 0L).apply()
        }
    }

    /**
     * US 3.2 - Restituisce le fasce orarie ricorrenti salvate.
     */
    override fun getSchedules(): List<DetoxSchedule> {
        val savedIds = prefs.getStringSet(KEY_ENABLED_SCHEDULE_IDS, null)
        return if (savedIds == null) {
            defaultSchedules
        } else {
            defaultSchedules.map { schedule ->
                schedule.copy(isEnabled = savedIds.contains(schedule.id))
            }
        }
    }

    override fun toggleSchedule(scheduleId: String): List<DetoxSchedule> {
        val updated = getSchedules().map { schedule ->
            if (schedule.id == scheduleId) {
                schedule.copy(isEnabled = !schedule.isEnabled)
            } else {
                schedule
            }
        }
        val enabledIds = updated.filter { it.isEnabled }.map { it.id }.toSet()
        prefs.edit().putStringSet(KEY_ENABLED_SCHEDULE_IDS, enabledIds).apply()

        // Se una fascia attiva copre l'orario attuale, attiva subito il blocco
        if (updated.any { it.isCurrentlyInWindow() }) {
            prefs.edit().putBoolean(KEY_DETOX_ACTIVE, true).apply()
        }
        syncScheduledAlarms()
        return updated
    }

    override fun isAnyScheduleCurrentlyActive(): Boolean {
        val now = Calendar.getInstance()
        return getSchedules().any { it.isCurrentlyInWindow(now) }
    }

    /**
     * US 3.2 - Calcola il prossimo inizio e la prossima fine delle fasce orarie attive
     * e li registra su `AlarmManager` di Android.
     */
    override fun syncScheduledAlarms() {
        val enabledSchedules = getSchedules().filter { it.isEnabled }
        if (enabledSchedules.isEmpty()) return

        val now = Calendar.getInstance()
        var nextStartMillis: Long? = null
        var nextEndMillis: Long? = null

        enabledSchedules.forEach { schedule ->
            val candidateStart = computeNextTransitionMillis(now, schedule.startHour, schedule.startMinute)
            val candidateEnd = computeNextTransitionMillis(now, schedule.endHour, schedule.endMinute)

            if (nextStartMillis == null || candidateStart < nextStartMillis!!) {
                nextStartMillis = candidateStart
            }
            if (nextEndMillis == null || candidateEnd < nextEndMillis!!) {
                nextEndMillis = candidateEnd
            }
        }

        nextStartMillis?.let {
            scheduleSystemAlarm(
                triggerAtMillis = it,
                action = DetoxAlarmReceiver.ACTION_ALARM_START_DETOX,
                requestCode = REQ_CODE_SCHEDULE_START
            )
        }
        nextEndMillis?.let {
            scheduleSystemAlarm(
                triggerAtMillis = it,
                action = DetoxAlarmReceiver.ACTION_ALARM_END_DETOX,
                requestCode = REQ_CODE_SCHEDULE_END
            )
        }
    }

    private fun computeNextTransitionMillis(now: Calendar, targetHour: Int, targetMinute: Int): Long {
        val cal = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= now.timeInMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun scheduleSystemAlarm(triggerAtMillis: Long, action: String, requestCode: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, DetoxAlarmReceiver::class.java).apply {
                this.action = action
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            // Ignora in ambienti di preview/test privi di AlarmManager
        }
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

    /**
     * US 4.1 - Contatore giornaliero delle Pause di Emergenza (si azzera ogni giorno a mezzanotte).
     */
    override fun getUsedEmergencyPausesToday(): Int {
        val todayTag = currentDateTag()
        val savedDay = prefs.getString(KEY_EMERGENCY_PAUSE_DATE, null)
        if (savedDay != todayTag) {
            prefs.edit()
                .putString(KEY_EMERGENCY_PAUSE_DATE, todayTag)
                .putInt(KEY_EMERGENCY_PAUSE_USED_COUNT, 0)
                .apply()
            return 0
        }
        return prefs.getInt(KEY_EMERGENCY_PAUSE_USED_COUNT, 0)
    }

    override fun getMaxDailyEmergencyPauses(): Int = MAX_DAILY_EMERGENCY_PAUSES

    override fun getRemainingEmergencyPauseMinutes(): Int {
        val endTimeMs = prefs.getLong(KEY_EMERGENCY_PAUSE_END_MS, 0L)
        val remainingMs = endTimeMs - System.currentTimeMillis()
        return if (remainingMs > 0L) {
            ((remainingMs + 59_999L) / 60_000L).toInt()
        } else {
            0
        }
    }

    override fun clearExpiredEmergencyPauseIfNeeded() {
        val endTimeMs = prefs.getLong(KEY_EMERGENCY_PAUSE_END_MS, 0L)
        if (endTimeMs > 0L && System.currentTimeMillis() >= endTimeMs) {
            prefs.edit().putLong(KEY_EMERGENCY_PAUSE_END_MS, 0L).apply()
        }
    }

    /**
     * US 4.1 - Avvia la Pausa di Emergenza di 5 minuti se il limite giornaliero (max 2/giorno) non è esaurito.
     */
    override fun startEmergencyPause(): Boolean {
        val usedToday = getUsedEmergencyPausesToday()
        if (usedToday >= MAX_DAILY_EMERGENCY_PAUSES) {
            return false
        }
        val endTimeMs = System.currentTimeMillis() + EMERGENCY_PAUSE_MINUTES * 60_000L
        prefs.edit()
            .putString(KEY_EMERGENCY_PAUSE_DATE, currentDateTag())
            .putInt(KEY_EMERGENCY_PAUSE_USED_COUNT, usedToday + 1)
            .putLong(KEY_EMERGENCY_PAUSE_END_MS, endTimeMs)
            .apply()

        scheduleSystemAlarm(
            triggerAtMillis = endTimeMs,
            action = DetoxAlarmReceiver.ACTION_ALARM_PAUSE_EXPIRED,
            requestCode = REQ_CODE_EMERGENCY_PAUSE
        )
        return true
    }

    /**
     * US 4.3 - Restituisce o genera il Codice Custode alfanumerico di 20 caratteri
     * (`XXXX-XXXX-XXXX-XXXX-XXXX`).
     */
    override fun getOrCreateCustodianCode(): String {
        val existing = prefs.getString(KEY_CUSTODIAN_CODE_20, null)
        if (!existing.isNullOrBlank()) {
            return existing
        }
        val generated = CustodianCodeStrategy.generate20CharCode()
        prefs.edit().putString(KEY_CUSTODIAN_CODE_20, generated).apply()
        return generated
    }

    override fun regenerateCustodianCode(): String {
        val generated = CustodianCodeStrategy.generate20CharCode()
        prefs.edit().putString(KEY_CUSTODIAN_CODE_20, generated).apply()
        return generated
    }

    override fun isCustodianUnlocked(): Boolean {
        return getRemainingCustodianUnlockMinutes() > 0
    }

    override fun getRemainingCustodianUnlockMinutes(): Int {
        val untilMs = prefs.getLong(KEY_CUSTODIAN_UNLOCK_UNTIL_MS, 0L)
        val remainingMs = untilMs - System.currentTimeMillis()
        return if (remainingMs > 0L) {
            ((remainingMs + 59_999L) / 60_000L).toInt()
        } else {
            0
        }
    }

    override fun grantCustodianUnlock(durationMinutes: Int) {
        val untilMs = System.currentTimeMillis() + durationMinutes * 60_000L
        prefs.edit().putLong(KEY_CUSTODIAN_UNLOCK_UNTIL_MS, untilMs).apply()
    }

    private fun currentDateTag(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    companion object {
        private const val PREFS_NAME = "modalita_nokia_prefs"
        private const val KEY_ENABLED_APP_IDS = "enabled_whitelist_app_ids"
        private const val KEY_ENABLED_SCHEDULE_IDS = "enabled_schedule_ids"
        private const val KEY_QUICK_TIMER_END_MS = "quick_timer_end_ms"
        private const val KEY_PALETTE_NAME = "selected_lcd_palette"
        private const val KEY_DETOX_ACTIVE = "detox_blocking_active"
        private const val KEY_SETTINGS_GRACE_UNTIL_MS = "settings_grace_until_ms"
        private const val KEY_LAST_BLOCKED_APP = "last_blocked_app_name"
        private const val KEY_BLOCKED_COUNT = "blocked_attempts_count"

        private const val KEY_EMERGENCY_PAUSE_DATE = "emergency_pause_date_tag"
        private const val KEY_EMERGENCY_PAUSE_USED_COUNT = "emergency_pause_used_count"
        private const val KEY_EMERGENCY_PAUSE_END_MS = "emergency_pause_end_ms"
        private const val KEY_CUSTODIAN_CODE_20 = "custodian_code_20_chars"
        private const val KEY_CUSTODIAN_UNLOCK_UNTIL_MS = "custodian_unlock_until_ms"

        const val MAX_DAILY_EMERGENCY_PAUSES = 2
        const val EMERGENCY_PAUSE_MINUTES = 5

        private const val REQ_CODE_QUICK_TIMER = 1001
        private const val REQ_CODE_SCHEDULE_START = 1002
        private const val REQ_CODE_SCHEDULE_END = 1003
        private const val REQ_CODE_EMERGENCY_PAUSE = 1004
    }
}
