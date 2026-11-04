package com.retrophonedetox.domain.model

/**
 * Rappresenta una singola voce all'interno del menu testuale sul display LCD Nokia.
 *
 * @property id Identificativo univoco della voce (es. "dialer", "messages", "whatsapp")
 * @property indexNumber Numero progressivo mostrato in alto a destra nello stile Nokia (es. 1, 2, 3...)
 * @property title Nome visualizzato sul display LCD (es. "Telefono", "Messaggi")
 * @property packageName Nome del pacchetto Android reale da lanciare (usato nella US 1.3), null per menu interni
 * @property description Breve testo mostrato nella barra inferiore o nella schermata di dettaglio
 */
data class NokiaMenuItem(
    val id: String,
    val indexNumber: Int,
    val title: String,
    val packageName: String? = null,
    val description: String
)
