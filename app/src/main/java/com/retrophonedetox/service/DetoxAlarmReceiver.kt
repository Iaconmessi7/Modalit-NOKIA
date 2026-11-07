package com.retrophonedetox.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.retrophonedetox.MainActivity
import com.retrophonedetox.data.repository.AndroidDetoxRepository

/**
 * US 3.1 & US 3.2 - Ricevitore di sistema (`BroadcastReceiver`) per l'automazione oraria.
 *
 * Viene svegliato da `AlarmManager` (o dal riavvio del telefono `BOOT_COMPLETED`):
 * 1. Quando inizia una fascia oraria programmata (`ACTION_ALARM_START_DETOX`), attiva il blocco
 *    e porta automaticamente in primo piano la schermata della Modalità NOKIA.
 * 2. Quando scade il timer rapido manuale (`ACTION_ALARM_TIMER_EXPIRED`) o termina una fascia
 *    oraria (`ACTION_ALARM_END_DETOX`), ricalcola lo stato e disattiva il blocco se non ci sono
 *    altre fasce attive, riprogrammando l'allarme successivo.
 */
class DetoxAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val repository = AndroidDetoxRepository(context.applicationContext)
        val action = intent?.action.orEmpty()

        when (action) {
            ACTION_ALARM_START_DETOX -> {
                repository.setDetoxBlockingActive(true)
                repository.syncScheduledAlarms()

                // Porta subito il telefono sulla schermata della Modalità NOKIA
                val launchNokia = Intent(context, MainActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                    putExtra(
                        DetoxGuardService.EXTRA_BLOCKED_BANNER,
                        "⏰ Fascia Detox iniziata automaticamente"
                    )
                }
                context.startActivity(launchNokia)
            }

            ACTION_ALARM_END_DETOX, ACTION_ALARM_TIMER_EXPIRED -> {
                repository.clearExpiredQuickTimerIfNeeded()
                val stillInScheduledWindow = repository.isAnyScheduleCurrentlyActive()
                repository.setDetoxBlockingActive(stillInScheduledWindow)
                repository.syncScheduledAlarms()
            }

            ACTION_ALARM_PAUSE_EXPIRED -> {
                // US 4.1: Scaduti i 5 minuti di Pausa SOS, riattiva automaticamente la Modalità NOKIA
                repository.clearExpiredEmergencyPauseIfNeeded()
                repository.setDetoxBlockingActive(true)
                val launchNokia = Intent(context, MainActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                    putExtra(
                        DetoxGuardService.EXTRA_BLOCKED_BANNER,
                        "🔒 Pausa SOS 5m terminata: Blocco riattivato"
                    )
                }
                context.startActivity(launchNokia)
            }

            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // Al riavvio dello smartphone, riprogramma tutte le sveglie Detox attive
                repository.clearExpiredQuickTimerIfNeeded()
                if (repository.isAnyScheduleCurrentlyActive() || repository.getRemainingQuickTimerMinutes() > 0) {
                    repository.setDetoxBlockingActive(true)
                }
                repository.syncScheduledAlarms()
            }
        }
    }

    companion object {
        const val ACTION_ALARM_START_DETOX = "com.retrophonedetox.action.ALARM_START_DETOX"
        const val ACTION_ALARM_END_DETOX = "com.retrophonedetox.action.ALARM_END_DETOX"
        const val ACTION_ALARM_TIMER_EXPIRED = "com.retrophonedetox.action.ALARM_TIMER_EXPIRED"
        const val ACTION_ALARM_PAUSE_EXPIRED = "com.retrophonedetox.action.ALARM_PAUSE_EXPIRED"
    }
}
