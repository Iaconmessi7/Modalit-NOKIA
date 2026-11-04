package com.retrophonedetox.domain.model

/**
 * Rappresenta i tasti fisici del tastierino Nokia (20% inferiore dello schermo).
 * Ogni pressione invia un evento al ViewModel seguendo il pattern MVVM + UDF.
 */
enum class NokiaKey {
    /** Freccia SU: scorre verso l'alto le voci del menu LCD */
    UP,

    /** Freccia GIÙ: scorre verso il basso le voci del menu LCD */
    DOWN,

    /** Tasto centrale OK / Navi-Key: conferma o apre la voce selezionata */
    OK,

    /** Tasto Verde Chiamata: azione rapida di conferma / avvio chiamata */
    CALL_GREEN,

    /** Tasto Rosso / C: torna indietro al menu precedente o chiude la schermata */
    BACK_RED
}
