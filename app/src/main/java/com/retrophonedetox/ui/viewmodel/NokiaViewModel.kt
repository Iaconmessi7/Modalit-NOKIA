package com.retrophonedetox.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.retrophonedetox.data.repository.AndroidDetoxRepository
import com.retrophonedetox.domain.model.AllowedApp
import com.retrophonedetox.domain.model.ConnectionOption
import com.retrophonedetox.domain.model.NokiaKey
import com.retrophonedetox.domain.model.NokiaMenuItem
import com.retrophonedetox.domain.repository.AppLaunchResult
import com.retrophonedetox.domain.repository.DetoxRepository
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
    val configurableApps: List<AllowedApp> = emptyList(),
    val menuItems: List<NokiaMenuItem> = emptyList(),
    val selectedIndex: Int = 0,
    val openedItem: NokiaMenuItem? = null,
    val subMenuCursorIndex: Int = 0,
    val isDetoxActive: Boolean = true,
    val batteryBars: Int = 4,
    val signalBars: Int = 4,
    val currentTime: String = formatCurrentTime(),
    val statusBanner: String? = null,
    val palette: NokiaLcdPalette = NokiaLcdPalette.SAGE_LCD
)

/**
 * ViewModel principale della Modalità NOKIA (Sprint 1 completo: US 1.1, 1.2, 1.3, 1.4):
 * - Legge e salva la Whitelist delle app tramite `DetoxRepository` (US 1.3)
 * - Lancia le vere app Android installate tramite `Intent` quando si preme OK (US 1.3)
 * - Gestisce il sottomenu "Connessioni" (Wi-Fi, Rete/Dati, Bluetooth, Hotspot) (US 1.4)
 * - Gestisce il sottomenu "App Consentite" per aggiungere/rimuovere app dal menu del Nokia (US 1.3)
 */
class NokiaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: DetoxRepository = AndroidDetoxRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<NokiaUiState> = _uiState.asStateFlow()

    private fun createInitialState(): NokiaUiState {
        val apps = repository.getConfigurableApps()
        val savedPalette = repository.getSavedPalette()
        val battery = repository.getBatteryBars()
        return NokiaUiState(
            configurableApps = apps,
            menuItems = buildNokiaMenuFromWhitelist(apps),
            batteryBars = battery,
            palette = savedPalette
        )
    }

    fun onKeyPress(key: NokiaKey) {
        _uiState.update { current ->
            val updatedTime = formatCurrentTime()
            val updatedBattery = repository.getBatteryBars()
            val themes = NokiaLcdPalette.entries
            val connectionOptions = ConnectionOption.entries

            when (key) {
                // Tasto in Alto a Destra (▲): scorre in su nel menu principale o nel sottomenu aperto
                NokiaKey.UP -> {
                    when (current.openedItem?.id) {
                        MENU_ID_CONNECTIONS -> {
                            val prev = if (current.subMenuCursorIndex - 1 < 0) {
                                connectionOptions.lastIndex
                            } else {
                                current.subMenuCursorIndex - 1
                            }
                            current.copy(
                                subMenuCursorIndex = prev,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = connectionOptions[prev].subtitle
                            )
                        }

                        MENU_ID_WHITELIST -> {
                            val prev = if (current.subMenuCursorIndex - 1 < 0) {
                                current.configurableApps.lastIndex
                            } else {
                                current.subMenuCursorIndex - 1
                            }
                            val app = current.configurableApps[prev]
                            current.copy(
                                subMenuCursorIndex = prev,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = if (app.isEssential) "App essenziale (sempre attiva)" else "Premi OK per attivare/disattivare"
                            )
                        }

                        MENU_ID_THEME -> {
                            val prev = if (current.subMenuCursorIndex - 1 < 0) {
                                themes.lastIndex
                            } else {
                                current.subMenuCursorIndex - 1
                            }
                            current.copy(
                                subMenuCursorIndex = prev,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = "Anteprima: ${themes[prev].displayName}"
                            )
                        }

                        null -> {
                            if (current.menuItems.isNotEmpty()) {
                                val prevIndex = if (current.selectedIndex - 1 < 0) {
                                    current.menuItems.lastIndex
                                } else {
                                    current.selectedIndex - 1
                                }
                                current.copy(
                                    selectedIndex = prevIndex,
                                    currentTime = updatedTime,
                                    batteryBars = updatedBattery,
                                    statusBanner = null
                                )
                            } else {
                                current.copy(currentTime = updatedTime, batteryBars = updatedBattery)
                            }
                        }

                        else -> current.copy(currentTime = updatedTime, batteryBars = updatedBattery)
                    }
                }

                // Tasto in Basso a Destra (▼): scorre in giù nel menu principale o nel sottomenu aperto
                NokiaKey.DOWN -> {
                    when (current.openedItem?.id) {
                        MENU_ID_CONNECTIONS -> {
                            val next = (current.subMenuCursorIndex + 1) % connectionOptions.size
                            current.copy(
                                subMenuCursorIndex = next,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = connectionOptions[next].subtitle
                            )
                        }

                        MENU_ID_WHITELIST -> {
                            val next = (current.subMenuCursorIndex + 1) % current.configurableApps.size
                            val app = current.configurableApps[next]
                            current.copy(
                                subMenuCursorIndex = next,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = if (app.isEssential) "App essenziale (sempre attiva)" else "Premi OK per attivare/disattivare"
                            )
                        }

                        MENU_ID_THEME -> {
                            val next = (current.subMenuCursorIndex + 1) % themes.size
                            current.copy(
                                subMenuCursorIndex = next,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = "Anteprima: ${themes[next].displayName}"
                            )
                        }

                        null -> {
                            if (current.menuItems.isNotEmpty()) {
                                val nextIndex = (current.selectedIndex + 1) % current.menuItems.size
                                current.copy(
                                    selectedIndex = nextIndex,
                                    currentTime = updatedTime,
                                    batteryBars = updatedBattery,
                                    statusBanner = null
                                )
                            } else {
                                current.copy(currentTime = updatedTime, batteryBars = updatedBattery)
                            }
                        }

                        else -> current.copy(currentTime = updatedTime, batteryBars = updatedBattery)
                    }
                }

                // Tasto Centrale OK: esegue l'azione nel menu o sottomenu
                NokiaKey.OK -> {
                    when (current.openedItem?.id) {
                        // US 1.4: Dentro "Connessioni", premendo OK apriamo il pannello rapido scelto
                        MENU_ID_CONNECTIONS -> {
                            val chosenOption = connectionOptions[current.subMenuCursorIndex]
                            val opened = repository.openConnectionControl(chosenOption)
                            current.copy(
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = if (opened) {
                                    "Aperto: ${chosenOption.title}"
                                } else {
                                    "Impossibile aprire ${chosenOption.title}"
                                }
                            )
                        }

                        // US 1.3: Dentro "App Consentite", premendo OK attiviamo/disattiviamo l'app nella Whitelist
                        MENU_ID_WHITELIST -> {
                            val targetApp = current.configurableApps.getOrNull(current.subMenuCursorIndex)
                            if (targetApp != null && !targetApp.isEssential) {
                                val updatedApps = repository.toggleAppWhitelist(targetApp.id)
                                val rebuiltMenu = buildNokiaMenuFromWhitelist(updatedApps)
                                val toggledApp = updatedApps.first { it.id == targetApp.id }
                                current.copy(
                                    configurableApps = updatedApps,
                                    menuItems = rebuiltMenu,
                                    currentTime = updatedTime,
                                    batteryBars = updatedBattery,
                                    statusBanner = if (toggledApp.isEnabled) {
                                        "Aggiunta al menu: ${toggledApp.title}"
                                    } else {
                                        "Rimossa dal menu: ${toggledApp.title}"
                                    }
                                )
                            } else {
                                current.copy(
                                    currentTime = updatedTime,
                                    batteryBars = updatedBattery,
                                    statusBanner = "Telefono e SMS sono sempre attivi!"
                                )
                            }
                        }

                        // Dentro "Tema Display", premendo OK attiviamo e salviamo il tema scelto
                        MENU_ID_THEME -> {
                            val chosenPalette = themes[current.subMenuCursorIndex]
                            repository.savePalette(chosenPalette)
                            current.copy(
                                palette = chosenPalette,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = "Tema salvato: ${chosenPalette.displayName}"
                            )
                        }

                        // Siamo nel Menu Principale del Nokia
                        null -> {
                            val selected = current.menuItems.getOrNull(current.selectedIndex)
                            if (selected == null) {
                                current
                            } else if (selected.packageName != null) {
                                // US 1.3: È un'app vera! La lanciamo subito tramite Intent di sistema!
                                val result = repository.launchRealApp(
                                    appId = selected.id,
                                    packageName = selected.packageName,
                                    title = selected.title
                                )
                                val banner = when (result) {
                                    is AppLaunchResult.Launched -> "Avvio ${result.appTitle}..."
                                    is AppLaunchResult.NotInstalled -> "${result.appTitle} non installata"
                                    is AppLaunchResult.Error -> result.message
                                }
                                current.copy(
                                    currentTime = updatedTime,
                                    batteryBars = updatedBattery,
                                    statusBanner = banner
                                )
                            } else {
                                // È un sottomenu interno del Nokia (Connessioni, App Consentite, Tema, Orari, SOS)
                                val initialSubCursor = if (selected.id == MENU_ID_THEME) {
                                    current.palette.ordinal
                                } else {
                                    0
                                }
                                current.copy(
                                    openedItem = selected,
                                    subMenuCursorIndex = initialSubCursor,
                                    currentTime = updatedTime,
                                    batteryBars = updatedBattery,
                                    statusBanner = selected.description
                                )
                            }
                        }

                        else -> current.copy(currentTime = updatedTime, batteryBars = updatedBattery)
                    }
                }

                // Tasto in Alto a Sinistra (SOFT_LEFT, ≡): riporta subito alla Home in cima
                NokiaKey.SOFT_LEFT -> {
                    current.copy(
                        openedItem = null,
                        selectedIndex = 0,
                        subMenuCursorIndex = 0,
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = "Home Nokia"
                    )
                }

                // Tasto in Basso a Sinistra (SOFT_RIGHT, C / ↩): torna indietro di un livello
                NokiaKey.SOFT_RIGHT -> {
                    if (current.openedItem != null) {
                        // Riallinea il cursore principale se la lista è cambiata
                        val safeSelectedIndex = current.selectedIndex.coerceAtMost(
                            (current.menuItems.size - 1).coerceAtLeast(0)
                        )
                        current.copy(
                            openedItem = null,
                            selectedIndex = safeSelectedIndex,
                            subMenuCursorIndex = 0,
                            currentTime = updatedTime,
                            batteryBars = updatedBattery,
                            statusBanner = null
                        )
                    } else {
                        current.copy(currentTime = updatedTime, batteryBars = updatedBattery)
                    }
                }
            }
        }
    }

    companion object {
        const val MENU_ID_CONNECTIONS = "connections"
        const val MENU_ID_WHITELIST = "whitelist"
        const val MENU_ID_SCHEDULE = "schedule"
        const val MENU_ID_EMERGENCY = "emergency"
        const val MENU_ID_THEME = "theme"
    }
}

private fun formatCurrentTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date())
}

/**
 * Costruisce dinamicamente il menu principale del Nokia combinando:
 * 1. Le vere app abilitate nella Whitelist (US 1.3)
 * 2. Le voci di sistema della Modalità NOKIA (Connessioni US 1.4, App Consentite US 1.3, Orari, SOS, Tema Display)
 */
fun buildNokiaMenuFromWhitelist(apps: List<AllowedApp>): List<NokiaMenuItem> {
    val enabledRealApps = apps.filter { it.isEnabled }
    val items = mutableListOf<NokiaMenuItem>()

    enabledRealApps.forEach { app ->
        items.add(
            NokiaMenuItem(
                id = app.id,
                indexNumber = items.size + 1,
                title = app.title,
                packageName = app.packageName,
                description = app.description
            )
        )
    }

    // Voci interne di sistema del Nokia
    items.add(
        NokiaMenuItem(
            id = NokiaViewModel.MENU_ID_CONNECTIONS,
            indexNumber = items.size + 1,
            title = "Connessioni",
            packageName = null,
            description = "Wi-Fi, Rete Dati, Bluetooth e Hotspot"
        )
    )
    items.add(
        NokiaMenuItem(
            id = NokiaViewModel.MENU_ID_WHITELIST,
            indexNumber = items.size + 1,
            title = "App Consentite",
            packageName = null,
            description = "Scegli quali app mostrare nel Nokia"
        )
    )
    items.add(
        NokiaMenuItem(
            id = NokiaViewModel.MENU_ID_SCHEDULE,
            indexNumber = items.size + 1,
            title = "Orari Detox",
            packageName = null,
            description = "Programma fasce orarie di blocco"
        )
    )
    items.add(
        NokiaMenuItem(
            id = NokiaViewModel.MENU_ID_EMERGENCY,
            indexNumber = items.size + 1,
            title = "Sblocco SOS",
            packageName = null,
            description = "Pausa emergenza (Enigma / Codice)"
        )
    )
    items.add(
        NokiaMenuItem(
            id = NokiaViewModel.MENU_ID_THEME,
            indexNumber = items.size + 1,
            title = "Tema Display",
            packageName = null,
            description = "Cambia colori Anti-Luce Blu (4 temi)"
        )
    )

    return items
}
