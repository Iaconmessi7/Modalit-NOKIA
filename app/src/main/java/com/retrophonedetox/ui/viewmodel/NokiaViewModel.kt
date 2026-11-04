package com.retrophonedetox.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.retrophonedetox.domain.model.NokiaKey
import com.retrophonedetox.domain.model.NokiaMenuItem
import com.retrophonedetox.ui.theme.NokiaLcdPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Stato immutabile della schermata Nokia (Pattern MVVM + Unidirectional Data Flow).
 */
data class NokiaUiState(
    val menuItems: List<NokiaMenuItem> = defaultNokiaMenu(),
    val selectedIndex: Int = 0,
    val openedItem: NokiaMenuItem? = null,
    val themeCursorIndex: Int = 0,
    val isDetoxActive: Boolean = true,
    val batteryBars: Int = 4,
    val signalBars: Int = 4,
    val currentTime: String = formatCurrentTime(),
    val statusBanner: String? = null,
    val palette: NokiaLcdPalette = NokiaLcdPalette.SAGE_LCD
)

/**
 * ViewModel della Modalità NOKIA:
 * - Anello SU (▲) / GIÙ (▼): scorre la lista corrente (Menu principale oppure i 4 Temi dentro "7. Tema Display")
 * - Tasto Centrale (OK): apre/conferma la voce selezionata
 * - Tasto in alto a sinistra (SOFT_LEFT, trattino blu + ≡): riporta subito alla Home (menu principale in cima)
 * - Tasto in alto a destra (SOFT_RIGHT, trattino blu + C / ↩): torna indietro dal sottomenu
 * - Tasto in basso a sinistra (CALL_GREEN, trattino verde + ✆): da qualsiasi schermata apre subito "1. Telefono"
 * - Tasto in basso a destra (END_RED, trattino rosso + ⏻): mantenuto graficamente così com'è, riservato a usi futuri
 */
class NokiaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NokiaUiState())
    val uiState: StateFlow<NokiaUiState> = _uiState.asStateFlow()

    fun onKeyPress(key: NokiaKey) {
        _uiState.update { current ->
            val updatedTime = formatCurrentTime()
            val themes = NokiaLcdPalette.entries

            when (key) {
                NokiaKey.UP -> {
                    when {
                        current.openedItem?.id == "theme" -> {
                            val prevThemeIdx = if (current.themeCursorIndex - 1 < 0) {
                                themes.lastIndex
                            } else {
                                current.themeCursorIndex - 1
                            }
                            current.copy(
                                themeCursorIndex = prevThemeIdx,
                                currentTime = updatedTime,
                                statusBanner = "Anteprima: ${themes[prevThemeIdx].displayName}"
                            )
                        }

                        current.openedItem == null && current.menuItems.isNotEmpty() -> {
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
                        }

                        else -> current.copy(currentTime = updatedTime)
                    }
                }

                NokiaKey.DOWN -> {
                    when {
                        current.openedItem?.id == "theme" -> {
                            val nextThemeIdx = (current.themeCursorIndex + 1) % themes.size
                            current.copy(
                                themeCursorIndex = nextThemeIdx,
                                currentTime = updatedTime,
                                statusBanner = "Anteprima: ${themes[nextThemeIdx].displayName}"
                            )
                        }

                        current.openedItem == null && current.menuItems.isNotEmpty() -> {
                            val nextIndex = (current.selectedIndex + 1) % current.menuItems.size
                            current.copy(
                                selectedIndex = nextIndex,
                                currentTime = updatedTime,
                                statusBanner = null
                            )
                        }

                        else -> current.copy(currentTime = updatedTime)
                    }
                }

                // Tasto Centrale OK: conferma / apre la voce selezionata
                NokiaKey.OK -> {
                    when {
                        current.openedItem?.id == "theme" -> {
                            val chosenPalette = themes[current.themeCursorIndex]
                            current.copy(
                                palette = chosenPalette,
                                currentTime = updatedTime,
                                statusBanner = "Attivato: ${chosenPalette.displayName}"
                            )
                        }

                        current.openedItem == null -> {
                            val selected = current.menuItems.getOrNull(current.selectedIndex)
                            current.copy(
                                openedItem = selected,
                                themeCursorIndex = current.palette.ordinal,
                                currentTime = updatedTime,
                                statusBanner = selected?.let {
                                    if (it.id == "theme") "Usa Su/Giù e premi OK"
                                    else "Apertura: ${it.title}"
                                }
                            )
                        }

                        else -> current.copy(currentTime = updatedTime)
                    }
                }

                // Tasto in alto a sinistra (SOFT_LEFT, ≡): riporta subito alla Home (menu principale in cima)
                NokiaKey.SOFT_LEFT -> {
                    current.copy(
                        openedItem = null,
                        selectedIndex = 0,
                        currentTime = updatedTime,
                        statusBanner = "Home Nokia"
                    )
                }

                // Tasto in alto a destra (SOFT_RIGHT, C / ↩): torna indietro di un livello
                NokiaKey.SOFT_RIGHT -> {
                    if (current.openedItem != null) {
                        current.copy(
                            openedItem = null,
                            currentTime = updatedTime,
                            statusBanner = null
                        )
                    } else {
                        current.copy(currentTime = updatedTime)
                    }
                }

                // Tasto in basso a sinistra (CALL_GREEN, ✆): apre subito le Chiamate / Telefono
                NokiaKey.CALL_GREEN -> {
                    val phoneItem = current.menuItems.firstOrNull { it.id == "phone" }
                    current.copy(
                        selectedIndex = 0,
                        openedItem = phoneItem,
                        currentTime = updatedTime,
                        statusBanner = "Chiamata rapida: Telefono"
                    )
                }

                // Tasto in basso a destra (END_RED, ⏻): lasciato così com'è, vedremo nei prossimi Sprint come sfruttarlo
                NokiaKey.END_RED -> {
                    current.copy(currentTime = updatedTime)
                }
            }
        }
    }
}

private fun formatCurrentTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date())
}

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
    ),
    NokiaMenuItem(
        id = "theme",
        indexNumber = 7,
        title = "Tema Display",
        packageName = null,
        description = "Cambia colori Anti-Luce Blu (4 temi)"
    )
)
