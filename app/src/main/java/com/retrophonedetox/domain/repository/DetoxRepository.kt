package com.retrophonedetox.domain.repository

import com.retrophonedetox.domain.model.AllowedApp
import com.retrophonedetox.domain.model.ConnectionOption
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
 * Punto centralizzato per leggere le app installate, salvare la Whitelist,
 * lanciare le vere app del telefono (US 1.3), aprire i controlli di connessione (US 1.4)
 * e coordinare il "Portiere" `DetoxGuardService` (US 2.1 & US 2.2).
 */
interface DetoxRepository {
    /** Restituisce la lista delle app configurabili nella Whitelist con il loro stato attivo/disattivo */
    fun getConfigurableApps(): List<AllowedApp>

    /** Attiva o disattiva un'app nella Whitelist del Nokia e salva la preferenza */
    fun toggleAppWhitelist(appId: String): List<AllowedApp>

    /** Restituisce l'insieme dei packageName attualmente autorizzati (usato dal Portiere nello Sprint 2) */
    fun getWhitelistedPackages(): Set<String>

    /** Lancia l'app reale sul telefono tramite Intent Android (US 1.3) */
    fun launchRealApp(appId: String, packageName: String, title: String): AppLaunchResult

    /** Apre il controllo rapido di sistema per Wi-Fi, Rete, Bluetooth o Hotspot (US 1.4) */
    fun openConnectionControl(option: ConnectionOption): Boolean

    /** US 2.1 & US 2.2 - Stato di attivazione del blocco "Portiere" */
    fun isDetoxBlockingActive(): Boolean
    fun setDetoxBlockingActive(active: Boolean)

    /** Verifica se l'utente ha abilitato il permesso AccessibilityService per `DetoxGuardService` */
    fun isGuardServiceEnabled(): Boolean

    /** Apre la schermata Accessibilità di Android per attivare il permesso del Portiere */
    fun openAccessibilityPermissionSettings(): Boolean

    /** Statistiche degli ultimi tentativi bloccati dal Portiere (mostrati sul display del Nokia) */
    fun recordLastBlockedApp(appName: String)
    fun getLastBlockedApp(): String?
    fun getBlockedAttemptsCount(): Int

    /** Legge il livello reale della batteria del telefono convertito in 1..4 tacche Nokia */
    fun getBatteryBars(): Int

    /** Legge e salva il tema Anti-Luce Blu scelto dall'utente */
    fun getSavedPalette(): NokiaLcdPalette
    fun savePalette(palette: NokiaLcdPalette)
}
