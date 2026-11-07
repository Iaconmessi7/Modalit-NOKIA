package com.retrophonedetox.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import com.retrophonedetox.MainActivity
import com.retrophonedetox.data.repository.AndroidDetoxRepository

/**
 * US 2.1 & US 2.2 - Il "Portiere" (`DetoxGuardService`).
 *
 * Questo servizio di sistema Android (`AccessibilityService`) resta in ascolto dei cambi
 * di finestra (`TYPE_WINDOW_STATE_CHANGED`):
 * 1. US 2.1: Se la Modalità NOKIA è attiva e l'utente prova ad aprire un'app non presente
 *    nella Whitelist (es. Instagram, TikTok, Chrome) o preme il tasto Home di Android per
 *    tornare al launcher standard, il Portiere intercetta l'apertura e riporta istantaneamente
 *    l'utente sulla schermata della Modalità NOKIA.
 * 2. US 2.2: Blocca l'apertura delle Impostazioni generali di Android (per impedire la
 *    disinstallazione o l'arresto forzato dell'app durante il blocco), ma permette l'uso dei
 *    4 pannelli rapidi di Connessione (Wi-Fi, Dati, Bluetooth, Hotspot) quando aperti dal menu
 *    interno del Nokia. Chiude inoltre automaticamente la tendina delle notifiche.
 */
class DetoxGuardService : AccessibilityService() {

    private lateinit var repository: AndroidDetoxRepository

    override fun onCreate() {
        super.onCreate()
        repository = AndroidDetoxRepository(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Se la modalità Detox non è attiva (es. Inactive o Pausa SOS), il Portiere lascia passare tutto
        if (!repository.isDetoxBlockingActive()) return

        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString().orEmpty()

        // 1. La nostra stessa app (Modalità NOKIA) è ovviamente sempre consentita
        if (packageName == applicationContext.packageName) return

        // 2. Consentiamo sempre le schermate di chiamata telefonica attiva/in arrivo e le tastiere (IME)
        if (isSystemCallOrKeyboardPackage(packageName)) return

        // 3. US 2.2 - Protezione Tendina Notifiche (SystemUI)
        if (packageName == SYSTEM_UI_PACKAGE) {
            if (isNotificationShadeOrQuickSettings(className, event)) {
                collapseNotificationShade()
                repository.recordLastBlockedApp("Tendina Notifiche")
                bringNokiaToFront("Bloccata: Tendina Notifiche")
            }
            return
        }

        // 4. US 2.2 - Protezione Impostazioni di Sistema (com.android.settings)
        if (packageName == SETTINGS_PACKAGE) {
            // Controlliamo se l'utente ha appena aperto il pannello Connessioni dal menu del Nokia (US 1.4)
            // e se non si trova nella pagina di disinstallazione/info app o accessibilità
            if (repository.isConnectionSettingsTemporarilyAllowed() && !isDangerousSettingsScreen(className)) {
                return
            } else {
                repository.recordLastBlockedApp("Impostazioni Android")
                bringNokiaToFront("Bloccate: Impostazioni Sistema")
                return
            }
        }

        // 5. US 2.1 - Controllo Whitelist per tutte le altre app e il Launcher standard
        val whitelistedPackages = repository.getWhitelistedPackages()
        if (!whitelistedPackages.contains(packageName)) {
            val readableName = repository.resolveReadableAppName(packageName)
            repository.recordLastBlockedApp(readableName)
            bringNokiaToFront("Bloccata: $readableName")
        }
    }

    override fun onInterrupt() {
        // Richiesto dal contratto AccessibilityService
    }

    /**
     * Chiude immediatamente la tendina delle notifiche / Quick Settings (US 2.2).
     */
    private fun collapseNotificationShade() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
        } else {
            @Suppress("DEPRECATION")
            sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        }
    }

    /**
     * Riporta istantaneamente l'utente sulla schermata principale della Modalità NOKIA (US 2.1).
     */
    private fun bringNokiaToFront(reasonBanner: String) {
        val nokiaIntent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(EXTRA_BLOCKED_BANNER, reasonBanner)
        }
        applicationContext.startActivity(nokiaIntent)
    }

    /**
     * Verifica se il pacchetto appartiene al sistema di chiamata telefonica o alla tastiera di scrittura,
     * così da non interrompere mai una telefonata o la scrittura di un messaggio WhatsApp/SMS.
     */
    private fun isSystemCallOrKeyboardPackage(pkg: String): Boolean {
        if (pkg in ESSENTIAL_CALL_PACKAGES) return true

        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        val enabledInputMethods = imm?.enabledInputMethodList?.map { it.packageName } ?: emptyList()
        if (pkg in enabledInputMethods) return true

        // Permessi di runtime di sistema (es. quando un'app autorizzata chiede il microfono)
        if (pkg == "com.google.android.permissioncontroller" || pkg == "com.android.permissioncontroller") {
            return true
        }

        return false
    }

    /**
     * Riconosce se l'evento di SystemUI è l'apertura della tendina delle notifiche / pannello rapido.
     */
    private fun isNotificationShadeOrQuickSettings(
        className: String,
        event: AccessibilityEvent
    ): Boolean {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return false
        val lower = className.lowercase()
        return lower.contains("statusbar") ||
            lower.contains("notification") ||
            lower.contains("qspanel") ||
            lower.contains("quicksettings") ||
            lower.contains("shade")
    }

    /**
     * Anche durante la finestra temporanea per Wi-Fi/Bluetooth (US 1.4), impedisce comunque
     * di aprire le schermate "Info App / Disinstalla" o "Accessibilità".
     */
    private fun isDangerousSettingsScreen(className: String): Boolean {
        val lower = className.lowercase()
        return lower.contains("installedapp") ||
            lower.contains("applicationsettings") ||
            lower.contains("accessibility") ||
            lower.contains("deviceadmin") ||
            lower.contains(" uninstaller")
    }

    companion object {
        const val EXTRA_BLOCKED_BANNER = "extra_blocked_banner"
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"
        private const val SETTINGS_PACKAGE = "com.android.settings"

        private val ESSENTIAL_CALL_PACKAGES = setOf(
            "com.android.incallui",
            "com.android.server.telecom",
            "com.google.android.dialer",
            "com.samsung.android.incallui",
            "com.samsung.android.dialer",
            "com.android.phone"
        )
    }
}
