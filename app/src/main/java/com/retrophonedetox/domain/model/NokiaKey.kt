package com.retrophonedetox.domain.model

/**
 * Rappresenta i 5 tasti fisici ergonomici del tastierino Nokia (20% inferiore dello schermo):
 * - Tasto Centrale: solo "OK" (nessun tocco accidentale Su/Giù vicino a OK)
 * - Alto a Destra: Freccia in Alto (▲) per scorrere il menu in su
 * - Basso a Destra: Freccia in Basso (▼) per scorrere il menu in giù
 * - Alto a Sinistra: Home (trattino blu + ≡)
 * - Basso a Sinistra: Torna Indietro (C / ↩)
 */
enum class NokiaKey {
    /** Tasto in Alto a Destra (▲): scorre verso l'alto */
    UP,

    /** Tasto in Basso a Destra (▼): scorre verso il basso */
    DOWN,

    /** Pulsante centrale "OK": conferma / apre la voce selezionata */
    OK,

    /** Tasto in Alto a Sinistra (trattino blu + ≡): riporta subito alla Home */
    SOFT_LEFT,

    /** Tasto in Basso a Sinistra (C / ↩): torna indietro di un livello */
    SOFT_RIGHT
}
