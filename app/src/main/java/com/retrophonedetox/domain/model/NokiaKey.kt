package com.retrophonedetox.domain.model

/**
 * Rappresenta i tasti fisici del tastierino Nokia anni 2000 (20% inferiore dello schermo).
 * Ogni pressione invia un evento al ViewModel seguendo il pattern MVVM + UDF.
 */
enum class NokiaKey {
    /** Corona superiore dell'anello centrale (Navi-Key): scorre verso l'alto */
    UP,

    /** Corona inferiore dell'anello centrale (Navi-Key): scorre verso il basso */
    DOWN,

    /** Pulsante quadrato centrale "OK": conferma / apre la voce selezionata */
    OK,

    /** Tasto Funzione Sinistro (trattino blu in alto a sinistra): esegue "Seleziona" (in basso a sx sullo schermo) */
    SOFT_LEFT,

    /** Tasto Funzione Destro (trattino blu in alto a destra): esegue "Indietro" (in basso a dx sullo schermo) */
    SOFT_RIGHT,

    /** Tasto Verde (trattino verde in basso a sinistra): da qualsiasi schermata apre subito Telefono / Chiamate */
    CALL_GREEN,

    /** Tasto Rosso (trattino rosso in basso a destra): da qualsiasi sottomenu riporta subito alla Home in cima */
    END_RED
}
