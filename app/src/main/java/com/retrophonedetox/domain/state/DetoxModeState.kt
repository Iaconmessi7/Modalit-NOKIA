package com.retrophonedetox.domain.state

/**
 * State Pattern (ARCHITECTURE.md):
 * Rappresenta gli stati operativi della Modalità NOKIA e definisce per ciascuno
 * se il "Portiere" (`DetoxGuardService`) deve bloccare le app fuori dalla Whitelist.
 */
sealed class DetoxModeState(
    val stateLabel: String,
    val shouldBlockUnauthorizedApps: Boolean
) {
    /** 1. Telefono normale: nessun blocco attivo */
    data object Inactive : DetoxModeState(
        stateLabel = "SPENTA (Libero)",
        shouldBlockUnauthorizedApps = false
    )

    /** 2. Modalità NOKIA attiva: consentite solo le app nella Whitelist */
    data object ActiveDetox : DetoxModeState(
        stateLabel = "ATTIVA (Protetta)",
        shouldBlockUnauthorizedApps = true
    )

    /** 3. Pausa di emergenza temporanea (usata nello Sprint 4) */
    data class EmergencyPause(val remainingSeconds: Long) : DetoxModeState(
        stateLabel = "PAusa SOS (${remainingSeconds}s)",
        shouldBlockUnauthorizedApps = false
    )

    /** 4. Risoluzione enigma o inserimento codice Custode in corso (Sprint 4) */
    data object UnlockChallenge : DetoxModeState(
        stateLabel = "SFIDA SBLOCCO",
        shouldBlockUnauthorizedApps = true
    )
}
