package com.retrophonedetox.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.retrophonedetox.domain.model.NokiaKey
import com.retrophonedetox.domain.model.NokiaMenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Stato immutabile della schermata Nokia (Pattern MVVM + Unidirectional Data Flow).
 * La UI legge solo questo oggetto per sapere cosa disegnare sul display LCD.
 */
data class NokiaUiState(
    val menuItems: List<NokiaMenuItem> = defaultNokiaMenu(),
    val selectedIndex: Int = 0,
    val openedItem: NokiaMenuItem? = null,
    val isDetoxActive: Boolean = true,
    val batteryBars: Int = 4,
    val signalBars: Int = 4,
    val currentTime: String = formatCurrentTime(),
    val statusBanner: String? = null
)

/**
 * ViewModel principale che riceve gli eventi dei tasti fisici (`onKeyPress`)
 * e aggiorna lo stato del display LCD.
 */
class NokiaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NokiaUiState())
    val uiState: StateFlow<NokiaUiState> = _uiState.asStateFlow()

    /**
     * Gestisce la pressione di un tasto sul tastierino inferiore (20% dello schermo).
     */
    fun onKeyPress(key: NokiaKey) {
        _uiState.update { current ->
            val updatedTime = formatCurrentTime()

            when (key) {
                NokiaKey.UP -> {
                    // Se siamo nel menu principale, saliamo alla voce precedente (con effetto circolare)
                    if (current.openedItem == null && current.menuItems.isNotEmpty()) {
                        val prevIndex = if (current.selectedIndex - 1 < 0) {
                            current.menuItems.lastIndex
                        } else {
                            current.selectedIndex - 1
                        }
                        current.copy(
                            selectedIndex = prevIndex,
                            currentTime = updatedTime,
                            statusBanner = null
                        )
                    } else {
                        current.copy(currentTime = updatedTime)
                    }
                }

                NokiaKey.DOWN -> {
                    // Se siamo nel menu principale, scendiamo alla voce successiva (con effetto circolare)
                    if (current.openedItem == null && current.menuItems.isNotEmpty()) {
                        val nextIndex = (current.selectedIndex + 1) % current.menuItems.size
                        current.copy(
                            selectedIndex = nextIndex,
                            currentTime = updatedTime,
                            statusBanner = null
                        )
                    } else {
                        current.copy(currentTime = updatedTime)
                    }
                }

                NokiaKey.OK, NokiaKey.CALL_GREEN -> {
                    // Conferma/apre la voce selezionata (oppure torna al menu se è già aperta)
                    if (current.openedItem == null) {
                        val selected = current.menuItems.getOrNull(current.selectedIndex)
                        current.copy(
                            openedItem = selected,
                            currentTime = updatedTime,
                            statusBanner = selected?.let { "Apertura: ${it.title}" }
                        )
                    } else {
                        current.copy(currentTime = updatedTime)
                    }
                }

                NokiaKey.BACK_RED -> {
                    // Il tasto Rosso / C chiude la schermata aperta e riporta al menu principale
                    if (current.openedItem != null) {
                        current.copy(
                            openedItem = null,
                            currentTime = updatedTime,
                            statusBanner = null
                        )
                    } else {
                        // Se siamo già nel menu principale, riporta il cursore alla prima voce
                        current.copy(
                            selectedIndex = 0,
                            currentTime = updatedTime,
                            statusBanner = "Menu principale"
                        )
                    }
                }
            }
        }
    }
}

private fun formatCurrentTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date())
}

/**
 * Voci iniziali del menu Nokia (stile Modalità Super Risparmio Energetico).
 * Nelle prossime User Stories (US 1.3 e US 1.4) collegheremo queste voci alle vere app installate!
 */
private fun defaultNokiaMenu(): List<NokiaMenuItem> = listOf(
    NokiaMenuItem(
        id = "phone",
        indexNumber = 1,
        title = "Telefono",
        packageName = "com.android.dialer",
        description = "Apri l'app Telefono per chiamare"
    ),
    NokiaMenuItem(
        id = "messages",
        indexNumber = 2,
        title = "Messaggi",
        packageName = "com.google.android.apps.messaging",
        description = "Apri gli SMS del telefono"
    ),
    NokiaMenuItem(
        id = "whatsapp",
        indexNumber = 3,
        title = "WhatsApp",
        packageName = "com.whatsapp",
        description = "Apri WhatsApp (App autorizzata)"
    ),
    NokiaMenuItem(
        id = "connections",
        indexNumber = 4,
        title = "Connessioni",
        packageName = null,
        description = "Gestisci Wi-Fi, Bluetooth e Hotspot"
    ),
    NokiaMenuItem(
        id = "schedule",
        indexNumber = 5,
        title = "Orari Detox",
        packageName = null,
        description = "Imposta fasce orarie e App consentite"
    ),
    NokiaMenuItem(
        id = "emergency",
        indexNumber = 6,
        title = "Sblocco SOS",
        packageName = null,
        description = "Pausa emergenza (Enigma / Codice)"
    )
)
