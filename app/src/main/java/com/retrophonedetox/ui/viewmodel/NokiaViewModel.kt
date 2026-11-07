package com.retrophonedetox.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.retrophonedetox.data.repository.AndroidDetoxRepository
import com.retrophonedetox.domain.model.AllowedApp
import com.retrophonedetox.domain.model.ConnectionOption
import com.retrophonedetox.domain.model.DetoxSchedule
import com.retrophonedetox.domain.model.NokiaKey
import com.retrophonedetox.domain.model.NokiaMenuItem
import com.retrophonedetox.domain.model.QuickTimerPreset
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
 * Stato immutabile della schermata Nokia (Sprint 1 + Sprint 2 + Sprint 3).
 */
data class NokiaUiState(
    val configurableApps: List<AllowedApp> = emptyList(),
    val schedules: List<DetoxSchedule> = emptyList(),
    val remainingQuickTimerMinutes: Int = 0,
    val menuItems: List<NokiaMenuItem> = emptyList(),
    val selectedIndex: Int = 0,
    val openedItem: NokiaMenuItem? = null,
    val subMenuCursorIndex: Int = 0,
    val isDetoxActive: Boolean = true,
    val isGuardPermissionEnabled: Boolean = false,
    val blockedAttemptsCount: Int = 0,
    val lastBlockedApp: String? = null,
    val batteryBars: Int = 4,
    val signalBars: Int = 4,
    val currentTime: String = formatCurrentTime(),
    val statusBanner: String? = null,
    val palette: NokiaLcdPalette = NokiaLcdPalette.SAGE_LCD
)

/**
 * ViewModel principale della Modalità NOKIA (Sprint 1, 2 e 3):
 * Include anche US 3.1 (Sessioni rapide a tempo 30m / 1h / 2h) e US 3.2 (Fasce orarie automatiche stile Sveglia).
 */
class NokiaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: DetoxRepository = AndroidDetoxRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<NokiaUiState> = _uiState.asStateFlow()

    private fun createInitialState(): NokiaUiState {
        repository.syncScheduledAlarms()
        val apps = repository.getConfigurableApps()
        val schedules = repository.getSchedules()
        return NokiaUiState(
            configurableApps = apps,
            schedules = schedules,
            remainingQuickTimerMinutes = repository.getRemainingQuickTimerMinutes(),
            menuItems = buildNokiaMenuFromWhitelist(apps),
            isDetoxActive = repository.isDetoxBlockingActive(),
            isGuardPermissionEnabled = repository.isGuardServiceEnabled(),
            blockedAttemptsCount = repository.getBlockedAttemptsCount(),
            lastBlockedApp = repository.getLastBlockedApp(),
            batteryBars = repository.getBatteryBars(),
            palette = repository.getSavedPalette()
        )
    }

    fun refreshSystemStatus(blockedBanner: String? = null) {
        _uiState.update { current ->
            current.copy(
                schedules = repository.getSchedules(),
                remainingQuickTimerMinutes = repository.getRemainingQuickTimerMinutes(),
                isDetoxActive = repository.isDetoxBlockingActive(),
                isGuardPermissionEnabled = repository.isGuardServiceEnabled(),
                blockedAttemptsCount = repository.getBlockedAttemptsCount(),
                lastBlockedApp = repository.getLastBlockedApp(),
                batteryBars = repository.getBatteryBars(),
                currentTime = formatCurrentTime(),
                statusBanner = blockedBanner ?: current.statusBanner
            )
        }
    }

    fun onKeyPress(key: NokiaKey) {
        _uiState.update { current ->
            val updatedTime = formatCurrentTime()
            val updatedBattery = repository.getBatteryBars()
            val updatedTimerMinutes = repository.getRemainingQuickTimerMinutes()
            val themes = NokiaLcdPalette.entries
            val connectionOptions = ConnectionOption.entries
            val quickPresets = QuickTimerPreset.entries
            // Nel sottomenu "Orari Detox" ci sono 3 preset rapidi (US 3.1) + 4 fasce orarie (US 3.2) = 7 righe totali
            val totalScheduleMenuRows = quickPresets.size + current.schedules.size

            when (key) {
                // Tasto in Alto a Destra (▲): scorre in su
                NokiaKey.UP -> {
                    when (current.openedItem?.id) {
                        MENU_ID_SCHEDULE -> {
                            val prev = if (current.subMenuCursorIndex - 1 < 0) {
                                (totalScheduleMenuRows - 1).coerceAtLeast(0)
                            } else {
                                current.subMenuCursorIndex - 1
                            }
                            current.copy(
                                subMenuCursorIndex = prev,
                                remainingQuickTimerMinutes = updatedTimerMinutes,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = scheduleRowSubtitle(prev, quickPresets, current.schedules)
                            )
                        }

                        MENU_ID_GUARD -> {
                            val prev = if (current.subMenuCursorIndex - 1 < 0) 2 else current.subMenuCursorIndex - 1
                            current.copy(
                                subMenuCursorIndex = prev,
                                remainingQuickTimerMinutes = updatedTimerMinutes,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = guardOptionSubtitle(prev)
                            )
                        }

                        MENU_ID_CONNECTIONS -> {
                            val prev = if (current.subMenuCursorIndex - 1 < 0) {
                                connectionOptions.lastIndex
                            } else {
                                current.subMenuCursorIndex - 1
                            }
                            current.copy(
                                subMenuCursorIndex = prev,
                                remainingQuickTimerMinutes = updatedTimerMinutes,
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
                                remainingQuickTimerMinutes = updatedTimerMinutes,
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
                                remainingQuickTimerMinutes = updatedTimerMinutes,
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
                                    remainingQuickTimerMinutes = updatedTimerMinutes,
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

                // Tasto in Basso a Destra (▼): scorre in giù
                NokiaKey.DOWN -> {
                    when (current.openedItem?.id) {
                        MENU_ID_SCHEDULE -> {
                            val next = if (totalScheduleMenuRows > 0) {
                                (current.subMenuCursorIndex + 1) % totalScheduleMenuRows
                            } else 0
                            current.copy(
                                subMenuCursorIndex = next,
                                remainingQuickTimerMinutes = updatedTimerMinutes,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = scheduleRowSubtitle(next, quickPresets, current.schedules)
                            )
                        }

                        MENU_ID_GUARD -> {
                            val next = (current.subMenuCursorIndex + 1) % 3
                            current.copy(
                                subMenuCursorIndex = next,
                                remainingQuickTimerMinutes = updatedTimerMinutes,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = guardOptionSubtitle(next)
                            )
                        }

                        MENU_ID_CONNECTIONS -> {
                            val next = (current.subMenuCursorIndex + 1) % connectionOptions.size
                            current.copy(
                                subMenuCursorIndex = next,
                                remainingQuickTimerMinutes = updatedTimerMinutes,
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
                                remainingQuickTimerMinutes = updatedTimerMinutes,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = if (app.isEssential) "App essenziale (sempre attiva)" else "Premi OK per attivare/disattivare"
                            )
                        }

                        MENU_ID_THEME -> {
                            val next = (current.subMenuCursorIndex + 1) % themes.size
                            current.copy(
                                subMenuCursorIndex = next,
                                remainingQuickTimerMinutes = updatedTimerMinutes,
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
                                    remainingQuickTimerMinutes = updatedTimerMinutes,
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

                // Tasto Centrale OK
                NokiaKey.OK -> {
                    when (current.openedItem?.id) {
                        // US 3.1 & US 3.2: Sottomenu "Orari Detox"
                        MENU_ID_SCHEDULE -> {
                            if (current.subMenuCursorIndex < quickPresets.size) {
                                // US 3.1: Avvia una sessione manuale rapida (30m, 1h, 2h)
                                val chosenPreset = quickPresets[current.subMenuCursorIndex]
                                repository.startQuickDetoxTimer(chosenPreset.minutes)
                                current.copy(
                                    isDetoxActive = true,
                                    remainingQuickTimerMinutes = chosenPreset.minutes,
                                    currentTime = updatedTime,
                                    batteryBars = updatedBattery,
                                    statusBanner = "⏱ Timer avviato: ${chosenPreset.minutes} min!"
                                )
                            } else {
                                // US 3.2: Attiva / Disattiva una fascia oraria programmata (stile Sveglia)
                                val scheduleIdx = current.subMenuCursorIndex - quickPresets.size
                                val targetSchedule = current.schedules.getOrNull(scheduleIdx)
                                if (targetSchedule != null) {
                                    val updatedSchedules = repository.toggleSchedule(targetSchedule.id)
                                    val toggled = updatedSchedules.first { it.id == targetSchedule.id }
                                    current.copy(
                                        schedules = updatedSchedules,
                                        isDetoxActive = repository.isDetoxBlockingActive(),
                                        remainingQuickTimerMinutes = repository.getRemainingQuickTimerMinutes(),
                                        currentTime = updatedTime,
                                        batteryBars = updatedBattery,
                                        statusBanner = if (toggled.isEnabled) {
                                            "Sveglia Detox ATTIVA: ${toggled.title}"
                                        } else {
                                            "Sveglia Detox DISATTIVATA: ${toggled.title}"
                                        }
                                    )
                                } else {
                                    current
                                }
                            }
                        }

                        MENU_ID_GUARD -> {
                            when (current.subMenuCursorIndex) {
                                0 -> {
                                    val newActive = !current.isDetoxActive
                                    repository.setDetoxBlockingActive(newActive)
                                    current.copy(
                                        isDetoxActive = repository.isDetoxBlockingActive(),
                                        remainingQuickTimerMinutes = repository.getRemainingQuickTimerMinutes(),
                                        currentTime = updatedTime,
                                        batteryBars = updatedBattery,
                                        statusBanner = if (newActive) "Portiere ATTIVO: App bloccate" else "Portiere in PAUSA: Telefono libero"
                                    )
                                }
                                1 -> {
                                    repository.openAccessibilityPermissionSettings()
                                    current.copy(
                                        currentTime = updatedTime,
                                        batteryBars = updatedBattery,
                                        statusBanner = "Attiva 'Portiere Modalità NOKIA'"
                                    )
                                }
                                else -> {
                                    val last = current.lastBlockedApp ?: "Nessuna"
                                    current.copy(
                                        currentTime = updatedTime,
                                        batteryBars = updatedBattery,
                                        statusBanner = "Ultimo blocco: $last (${current.blockedAttemptsCount} tot)"
                                    )
                                }
                            }
                        }

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

                        null -> {
                            val selected = current.menuItems.getOrNull(current.selectedIndex)
                            if (selected == null) {
                                current
                            } else if (selected.packageName != null) {
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
                                val initialSubCursor = if (selected.id == MENU_ID_THEME) {
                                    current.palette.ordinal
                                } else {
                                    0
                                }
                                current.copy(
                                    openedItem = selected,
                                    subMenuCursorIndex = initialSubCursor,
                                    schedules = repository.getSchedules(),
                                    remainingQuickTimerMinutes = updatedTimerMinutes,
                                    isGuardPermissionEnabled = repository.isGuardServiceEnabled(),
                                    blockedAttemptsCount = repository.getBlockedAttemptsCount(),
                                    lastBlockedApp = repository.getLastBlockedApp(),
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
                        remainingQuickTimerMinutes = updatedTimerMinutes,
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = "Home Nokia"
                    )
                }

                // Tasto in Basso a Sinistra (SOFT_RIGHT, C / ↩): torna indietro di un livello
                NokiaKey.SOFT_RIGHT -> {
                    if (current.openedItem != null) {
                        val safeSelectedIndex = current.selectedIndex.coerceAtMost(
                            (current.menuItems.size - 1).coerceAtLeast(0)
                        )
                        current.copy(
                            openedItem = null,
                            selectedIndex = safeSelectedIndex,
                            subMenuCursorIndex = 0,
                            remainingQuickTimerMinutes = updatedTimerMinutes,
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

    private fun scheduleRowSubtitle(
        cursorIndex: Int,
        presets: List<QuickTimerPreset>,
        schedules: List<DetoxSchedule>
    ): String {
        return if (cursorIndex < presets.size) {
            presets[cursorIndex].description
        } else {
            val s = schedules.getOrNull(cursorIndex - presets.size)
            if (s != null) "Fascia automatica (${s.daysTag}) - OK=Attiva/Disattiva" else ""
        }
    }

    private fun guardOptionSubtitle(index: Int): String = when (index) {
        0 -> "Premi OK per attivare/disattivare il blocco"
        1 -> "Premi OK per aprire Permessi Accessibilità"
        else -> "Protezione Notifiche e Impostazioni (US 2.2)"
    }

    companion object {
        const val MENU_ID_CONNECTIONS = "connections"
        const val MENU_ID_WHITELIST = "whitelist"
        const val MENU_ID_GUARD = "guard"
        const val MENU_ID_SCHEDULE = "schedule"
        const val MENU_ID_EMERGENCY = "emergency"
        const val MENU_ID_THEME = "theme"
    }
}

private fun formatCurrentTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date())
}

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
            id = NokiaViewModel.MENU_ID_GUARD,
            indexNumber = items.size + 1,
            title = "Portiere Detox",
            packageName = null,
            description = "Blocco App, Impostazioni e Notifiche"
        )
    )
    items.add(
        NokiaMenuItem(
            id = NokiaViewModel.MENU_ID_SCHEDULE,
            indexNumber = items.size + 1,
            title = "Orari Detox",
            packageName = null,
            description = "Timer rapido e Fasce orarie sveglia"
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
