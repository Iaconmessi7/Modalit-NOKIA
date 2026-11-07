package com.retrophonedetox.domain.model

/**
 * US 1.4 - Voci del Pannello Utilità Connessioni interno al Nokia.
 * Permette di gestire Wi-Fi, Bluetooth, Hotspot e Dati Mobili tramite i pannelli rapidi
 * di sistema senza esporre l'intero menu Impostazioni di Android.
 */
enum class ConnectionOption(
    val indexNumber: Int,
    val title: String,
    val subtitle: String
) {
    WIFI_PANEL(
        indexNumber = 1,
        title = "Wi-Fi Rapido",
        subtitle = "Attiva/Disattiva Wi-Fi (Pannello rapido)"
    ),
    INTERNET_DATA(
        indexNumber = 2,
        title = "Rete & Dati",
        subtitle = "Pannello rapido Connessione Dati e Rete"
    ),
    BLUETOOTH(
        indexNumber = 3,
        title = "Bluetooth",
        subtitle = "Apri controllo diretto Bluetooth"
    ),
    HOTSPOT(
        indexNumber = 4,
        title = "Hotspot Wi-Fi",
        subtitle = "Apri controllo diretto Hotspot / Tethering"
    )
}
