package com.retrophonedetox.domain.model

/**
 * US 1.3 - Rappresenta un'applicazione del telefono che può essere inserita
 * nella Whitelist della Modalità NOKIA (stile Modalità Super Risparmio Energetico).
 *
 * @property id Identificativo interno (es. "phone", "messages", "whatsapp", "maps")
 * @property title Nome mostrato nel menu LCD del Nokia
 * @property packageName Nome del pacchetto Android (o azione speciale per Telefono/SMS)
 * @property description Descrizione mostrata nella barra inferiore dello schermo LCD
 * @property isEssential Se true (es. Telefono), è sempre attiva per sicurezza
 * @property isEnabled Se true, compare nel menu principale del Nokia e può essere aperta
 */
data class AllowedApp(
    val id: String,
    val title: String,
    val packageName: String,
    val description: String,
    val isEssential: Boolean = false,
    val isEnabled: Boolean = true
)
