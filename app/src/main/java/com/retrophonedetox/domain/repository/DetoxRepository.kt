package com.retrophonedetox.domain.repository

import com.retrophonedetox.domain.model.AllowedApp
import com.retrophonedetox.domain.model.ConnectionOption
import com.retrophonedetox.domain.model.DetoxSchedule
import com.retrophonedetox.ui.theme.NokiaLcdPalette

/**
 * Risultato del tentativo di apertura di un'app reale tramite Intent di sistema (US 1.3).
 */
sealed class AppLaunchResult {
    data class Launched(val appTitle: String) : AppLaunchResult()
    data class NotInstalled(val appTitle: String) : AppLaunchResult()
    data class Error(val message: String) : AppLaunchResult()
}

/**
 * Pattern Repository (ARCHITECTURE.md):
 * Punto centralizzato per Whitelist e avvio app reali (US 1.3), Connessioni (US 1.4),
 * Portiere `DetoxGuardService` (US 2.1 & 2.2) e Automazione Orari/Timer (US 3.1 & 3.2).
 */
interface DetoxRepository {
    /** US 1.3 - Gestione Whitelist e avvio App Reali */
    fun getConfigurableApps(): List<AllowedApp>
    fun toggleAppWhitelist(appId: String): List<AllowedApp>
    fun getWhitelistedPackages(): Set<String>
    fun launchRealApp(appId: String, packageName: String, title: String): AppLaunchResult

    /** US 1.4 - Controlli rapidi Connessioni */
    fun openConnectionControl(option: ConnectionOption): Boolean

    /** US 2.1 & US 2.2 - Stato e statistiche del "Portiere" */
    fun isDetoxBlockingActive(): Boolean
    fun setDetoxBlockingActive(active: Boolean)
    fun isGuardServiceEnabled(): Boolean
    fun openAccessibilityPermissionSettings(): Boolean
    fun recordLastBlockedApp(appName: String)
    fun getLastBlockedApp(): String?
    fun getBlockedAttemptsCount(): Int

    /** US 3.1 - Attivazione Manuale Rapida a Tempo (Timer) */
    fun startQuickDetoxTimer(durationMinutes: Int)
    fun getRemainingQuickTimerMinutes(): Int
    fun clearExpiredQuickTimerIfNeeded()

    /** US 3.2 - Programmazione a Fasce Orarie Ricorrenti (Stile Sveglia) */
    fun getSchedules(): List<DetoxSchedule>
    fun toggleSchedule(scheduleId: String): List<DetoxSchedule>
    fun isAnyScheduleCurrentlyActive(): Boolean
    fun syncScheduledAlarms()

    /** US 4.1 - Finestra di Pausa di Emergenza Limitata (5 min, max 2/giorno) */
    fun getUsedEmergencyPausesToday(): Int
    fun getMaxDailyEmergencyPauses(): Int
    fun getRemainingEmergencyPauseMinutes(): Int
    fun clearExpiredEmergencyPauseIfNeeded()
    fun startEmergencyPause(): Boolean

    /** US 4.3 - Il "Custodian System" (Codice Alfanumerico a 20 Caratteri) */
    fun getOrCreateCustodianCode(): String
    fun regenerateCustodianCode(): String
    fun isCustodianUnlocked(): Boolean
    fun getRemainingCustodianUnlockMinutes(): Int
    fun grantCustodianUnlock(durationMinutes: Int = 5)

    /** Stato hardware e preferenze tema Anti-Luce Blu */
    fun getBatteryBars(): Int
    fun getSavedPalette(): NokiaLcdPalette
    fun savePalette(palette: NokiaLcdPalette)
}
