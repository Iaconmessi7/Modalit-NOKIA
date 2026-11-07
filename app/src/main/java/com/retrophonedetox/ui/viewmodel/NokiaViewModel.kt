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
import com.retrophonedetox.domain.strategy.CustodianCodeChallenge
import com.retrophonedetox.domain.strategy.CustodianCodeStrategy
import com.retrophonedetox.domain.strategy.MathPuzzleChallenge
import com.retrophonedetox.domain.strategy.MathPuzzleStrategy
import com.retrophonedetox.domain.strategy.UnlockChallenge
import com.retrophonedetox.ui.theme.NokiaLcdPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Stato immutabile della schermata Nokia (Sprint 1 + Sprint 2 + Sprint 3 + Sprint 4).
 */
data class NokiaUiState(
    val configurableApps: List<AllowedApp> = emptyList(),
    val schedules: List<DetoxSchedule> = emptyList(),
    val remainingQuickTimerMinutes: Int = 0,
    val usedEmergencyPausesToday: Int = 0,
    val maxDailyEmergencyPauses: Int = 2,
    val remainingEmergencyPauseMinutes: Int = 0,
    val custodianCode20: String = "",
    val isCustodianUnlocked: Boolean = false,
    val remainingCustodianUnlockMinutes: Int = 0,
    val activeChallenge: UnlockChallenge? = null,
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
 * ViewModel principale della Modalità NOKIA (Sprint 1, 2, 3 e 4):
 * Include anche EPIC 4 (Sicurezza Anti-Sabotaggio):
 * - US 4.1: Finestra di Pausa di Emergenza limitata (5 min, max 2 al giorno)
 * - US 4.2: Enigmi Cognitivi di Sblocco (`MathPuzzleStrategy`)
 * - US 4.3: Il "Custodian System" con Codice Alfanumerico a 20 caratteri (`CustodianCodeStrategy`)
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
            usedEmergencyPausesToday = repository.getUsedEmergencyPausesToday(),
            maxDailyEmergencyPauses = repository.getMaxDailyEmergencyPauses(),
            remainingEmergencyPauseMinutes = repository.getRemainingEmergencyPauseMinutes(),
            custodianCode20 = repository.getOrCreateCustodianCode(),
            isCustodianUnlocked = repository.isCustodianUnlocked(),
            remainingCustodianUnlockMinutes = repository.getRemainingCustodianUnlockMinutes(),
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
                usedEmergencyPausesToday = repository.getUsedEmergencyPausesToday(),
                remainingEmergencyPauseMinutes = repository.getRemainingEmergencyPauseMinutes(),
                custodianCode20 = repository.getOrCreateCustodianCode(),
                isCustodianUnlocked = repository.isCustodianUnlocked(),
                remainingCustodianUnlockMinutes = repository.getRemainingCustodianUnlockMinutes(),
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
            val updatedPauseMinutes = repository.getRemainingEmergencyPauseMinutes()
            val updatedCustodianUnlocked = repository.isCustodianUnlocked()
            val updatedCustodianMinutes = repository.getRemainingCustodianUnlockMinutes()
            val updatedDetoxActive = repository.isDetoxBlockingActive()

            val themes = NokiaLcdPalette.entries
            val connectionOptions = ConnectionOption.entries
            val quickPresets = QuickTimerPreset.entries
            val totalScheduleMenuRows = quickPresets.size + current.schedules.size

            when (key) {
                // Tasto in Alto a Destra (▲): scorre in su
                NokiaKey.UP -> {
                    when (current.openedItem?.id) {
                        MENU_ID_EMERGENCY -> {
                            val totalOptions = current.activeChallenge?.options?.size ?: 4
                            val prev = if (current.subMenuCursorIndex - 1 < 0) {
                                totalOptions - 1
                            } else {
                                current.subMenuCursorIndex - 1
                            }
                            current.copy(
                                subMenuCursorIndex = prev,
                                remainingQuickTimerMinutes = updatedTimerMinutes,
                                remainingEmergencyPauseMinutes = updatedPauseMinutes,
                                isCustodianUnlocked = updatedCustodianUnlocked,
                                remainingCustodianUnlockMinutes = updatedCustodianMinutes,
                                isDetoxActive = updatedDetoxActive,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = if (current.activeChallenge != null) {
                                    "Seleziona con ▲/▼ e premi OK"
                                } else {
                                    emergencyRowSubtitle(prev, current.custodianCode20)
                                }
                            )
                        }

                        MENU_ID_SCHEDULE -> {
                            val prev = if (current.subMenuCursorIndex - 1 < 0) {
                                (totalScheduleMenuRows - 1).coerceAtLeast(0)
                            } else {
                                current.subMenuCursorIndex - 1
                            }
                            current.copy(
                                subMenuCursorIndex = prev,
                                remainingQuickTimerMinutes = updatedTimerMinutes,
                                remainingEmergencyPauseMinutes = updatedPauseMinutes,
                                isCustodianUnlocked = updatedCustodianUnlocked,
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
                                remainingEmergencyPauseMinutes = updatedPauseMinutes,
                                isCustodianUnlocked = updatedCustodianUnlocked,
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
                                isCustodianUnlocked = updatedCustodianUnlocked,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = if (app.isEssential) {
                                    "App essenziale (sempre attiva)"
                                } else if (updatedDetoxActive && !updatedCustodianUnlocked) {
                                    "🔒 Protetta: richiede Codice Custode 20 car."
                                } else {
                                    "Premi OK per attivare/disattivare"
                                }
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
                                    remainingEmergencyPauseMinutes = updatedPauseMinutes,
                                    isCustodianUnlocked = updatedCustodianUnlocked,
                                    isDetoxActive = updatedDetoxActive,
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
                        MENU_ID_EMERGENCY -> {
                            val totalOptions = current.activeChallenge?.options?.size ?: 4
                            val next = (current.subMenuCursorIndex + 1) % totalOptions
                            current.copy(
                                subMenuCursorIndex = next,
                                remainingQuickTimerMinutes = updatedTimerMinutes,
                                remainingEmergencyPauseMinutes = updatedPauseMinutes,
                                isCustodianUnlocked = updatedCustodianUnlocked,
                                remainingCustodianUnlockMinutes = updatedCustodianMinutes,
                                isDetoxActive = updatedDetoxActive,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = if (current.activeChallenge != null) {
                                    "Seleziona con ▲/▼ e premi OK"
                                } else {
                                    emergencyRowSubtitle(next, current.custodianCode20)
                                }
                            )
                        }

                        MENU_ID_SCHEDULE -> {
                            val next = if (totalScheduleMenuRows > 0) {
                                (current.subMenuCursorIndex + 1) % totalScheduleMenuRows
                            } else 0
                            current.copy(
                                subMenuCursorIndex = next,
                                remainingQuickTimerMinutes = updatedTimerMinutes,
                                remainingEmergencyPauseMinutes = updatedPauseMinutes,
                                isCustodianUnlocked = updatedCustodianUnlocked,
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
                                remainingEmergencyPauseMinutes = updatedPauseMinutes,
                                isCustodianUnlocked = updatedCustodianUnlocked,
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
                                isCustodianUnlocked = updatedCustodianUnlocked,
                                currentTime = updatedTime,
                                batteryBars = updatedBattery,
                                statusBanner = if (app.isEssential) {
                                    "App essenziale (sempre attiva)"
                                } else if (updatedDetoxActive && !updatedCustodianUnlocked) {
                                    "🔒 Protetta: richiede Codice Custode 20 car."
                                } else {
                                    "Premi OK per attivare/disattivare"
                                }
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
                                    remainingEmergencyPauseMinutes = updatedPauseMinutes,
                                    isCustodianUnlocked = updatedCustodianUnlocked,
                                    isDetoxActive = updatedDetoxActive,
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
                        // US 4.1, US 4.2 & US 4.3: Sottomenu "Sblocco SOS"
                        MENU_ID_EMERGENCY -> handleEmergencyMenuOk(
                            current = current,
                            updatedTime = updatedTime,
                            updatedBattery = updatedBattery
                        )

                        // US 3.1 & US 3.2: Sottomenu "Orari Detox"
                        MENU_ID_SCHEDULE -> {
                            if (current.subMenuCursorIndex < quickPresets.size) {
                                val chosenPreset = quickPresets[current.subMenuCursorIndex]
                                repository.startQuickDetoxTimer(chosenPreset.minutes)
                                current.copy(
                                    isDetoxActive = true,
                                    remainingQuickTimerMinutes = chosenPreset.minutes,
                                    remainingEmergencyPauseMinutes = 0,
                                    currentTime = updatedTime,
                                    batteryBars = updatedBattery,
                                    statusBanner = "⏱ Timer avviato: ${chosenPreset.minutes} min!"
                                )
                            } else {
                                val scheduleIdx = current.subMenuCursorIndex - quickPresets.size
                                val targetSchedule = current.schedules.getOrNull(scheduleIdx)
                                if (targetSchedule != null) {
                                    // US 4.3: Disattivare una fascia oraria attiva durante il blocco richiede il Codice Custode
                                    if (targetSchedule.isEnabled && updatedDetoxActive && !updatedCustodianUnlocked) {
                                        current.copy(
                                            currentTime = updatedTime,
                                            batteryBars = updatedBattery,
                                            statusBanner = "🔒 Protetta! Usa Codice 20 car. in Sblocco SOS"
                                        )
                                    } else {
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
                                    }
                                } else {
                                    current
                                }
                            }
                        }

                        MENU_ID_GUARD -> {
                            when (current.subMenuCursorIndex) {
                                0 -> {
                                    // US 4.3: Spegnere il Portiere mentre è attivo richiede lo Sblocco Custode o una Pausa SOS
                                    if (updatedDetoxActive && !updatedCustodianUnlocked) {
                                        current.copy(
                                            currentTime = updatedTime,
                                            batteryBars = updatedBattery,
                                            statusBanner = "🔒 Vai in '8. Sblocco SOS' per Pausa o Custode!"
                                        )
                                    } else {
                                        val newActive = !updatedDetoxActive
                                        repository.setDetoxBlockingActive(newActive)
                                        current.copy(
                                            isDetoxActive = repository.isDetoxBlockingActive(),
                                            remainingQuickTimerMinutes = repository.getRemainingQuickTimerMinutes(),
                                            currentTime = updatedTime,
                                            batteryBars = updatedBattery,
                                            statusBanner = if (newActive) "Portiere ATTIVO: App bloccate" else "Portiere in PAUSA: Telefono libero"
                                        )
                                    }
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
                            if (targetApp == null) {
                                current
                            } else if (targetApp.isEssential) {
                                current.copy(
                                    currentTime = updatedTime,
                                    batteryBars = updatedBattery,
                                    statusBanner = "Telefono e SMS sono sempre attivi!"
                                )
                            } else if (updatedDetoxActive && !updatedCustodianUnlocked) {
                                // US 4.3: Protezione anti-sabotaggio della Whitelist
                                current.copy(
                                    currentTime = updatedTime,
                                    batteryBars = updatedBattery,
                                    statusBanner = "🔒 Protetta! Sblocca col Codice 20 car. in Sblocco SOS"
                                )
                            } else {
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
                                    activeChallenge = null,
                                    schedules = repository.getSchedules(),
                                    remainingQuickTimerMinutes = updatedTimerMinutes,
                                    usedEmergencyPausesToday = repository.getUsedEmergencyPausesToday(),
                                    remainingEmergencyPauseMinutes = updatedPauseMinutes,
                                    custodianCode20 = repository.getOrCreateCustodianCode(),
                                    isCustodianUnlocked = updatedCustodianUnlocked,
                                    remainingCustodianUnlockMinutes = updatedCustodianMinutes,
                                    isDetoxActive = updatedDetoxActive,
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
                        activeChallenge = null,
                        selectedIndex = 0,
                        subMenuCursorIndex = 0,
                        remainingQuickTimerMinutes = updatedTimerMinutes,
                        remainingEmergencyPauseMinutes = updatedPauseMinutes,
                        isCustodianUnlocked = updatedCustodianUnlocked,
                        isDetoxActive = updatedDetoxActive,
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = "Home Nokia"
                    )
                }

                // Tasto in Basso a Sinistra (SOFT_RIGHT, C / ↩): torna indietro di un livello
                NokiaKey.SOFT_RIGHT -> {
                    if (current.activeChallenge != null) {
                        // Se era dentro un Enigma o verifica Codice Custode, torna al sottomenu Sblocco SOS
                        current.copy(
                            activeChallenge = null,
                            subMenuCursorIndex = 0,
                            currentTime = updatedTime,
                            batteryBars = updatedBattery,
                            statusBanner = "Sfida annullata"
                        )
                    } else if (current.openedItem != null) {
                        val safeSelectedIndex = current.selectedIndex.coerceAtMost(
                            (current.menuItems.size - 1).coerceAtLeast(0)
                        )
                        current.copy(
                            openedItem = null,
                            selectedIndex = safeSelectedIndex,
                            subMenuCursorIndex = 0,
                            remainingQuickTimerMinutes = updatedTimerMinutes,
                            remainingEmergencyPauseMinutes = updatedPauseMinutes,
                            isCustodianUnlocked = updatedCustodianUnlocked,
                            isDetoxActive = updatedDetoxActive,
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

    /**
     * Gestisce la pressione di `OK` dentro il sottomenu `8. Sblocco SOS` (US 4.1, US 4.2, US 4.3).
     */
    private fun handleEmergencyMenuOk(
        current: NokiaUiState,
        updatedTime: String,
        updatedBattery: Int
    ): NokiaUiState {
        val activeChallenge = current.activeChallenge

        // Caso A: L'utente sta risolvendo l'Enigma Matematico (US 4.2)
        if (activeChallenge is MathPuzzleChallenge) {
            val isCorrect = MathPuzzleStrategy.isSelectionCorrect(
                challenge = activeChallenge,
                selectedOptionIndex = current.subMenuCursorIndex
            )
            return if (isCorrect) {
                if (activeChallenge.currentStep < activeChallenge.totalSteps) {
                    val nextChallenge = MathPuzzleStrategy.generateChallenge(
                        step = activeChallenge.currentStep + 1,
                        totalSteps = activeChallenge.totalSteps
                    )
                    current.copy(
                        activeChallenge = nextChallenge,
                        subMenuCursorIndex = 0,
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = "✓ Esatto! Risolvi il calcolo ${nextChallenge.currentStep}/${nextChallenge.totalSteps}"
                    )
                } else {
                    // US 4.1: Enigma superato! Avvia la Pausa di Emergenza di 5 minuti
                    val started = repository.startEmergencyPause()
                    current.copy(
                        activeChallenge = null,
                        subMenuCursorIndex = 0,
                        usedEmergencyPausesToday = repository.getUsedEmergencyPausesToday(),
                        remainingEmergencyPauseMinutes = repository.getRemainingEmergencyPauseMinutes(),
                        isDetoxActive = repository.isDetoxBlockingActive(),
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = if (started) {
                            "🔓 Pausa SOS 5 min ATTIVA! (${repository.getUsedEmergencyPausesToday()}/2 oggi)"
                        } else {
                            "⛔ Limite 2 pause giornaliere raggiunto!"
                        }
                    )
                }
            } else {
                val retryChallenge = MathPuzzleStrategy.generateChallenge(
                    step = 1,
                    totalSteps = activeChallenge.totalSteps
                )
                current.copy(
                    activeChallenge = retryChallenge,
                    subMenuCursorIndex = 0,
                    currentTime = updatedTime,
                    batteryBars = updatedBattery,
                    statusBanner = "✗ Risposta errata! Nuovo calcolo 1/${retryChallenge.totalSteps}"
                )
            }
        }

        // Caso B: L'utente sta verificando il Codice Custode a 20 caratteri (US 4.3)
        if (activeChallenge is CustodianCodeChallenge) {
            val isCorrect = CustodianCodeStrategy.isSelectionCorrect(
                challenge = activeChallenge,
                selectedOptionIndex = current.subMenuCursorIndex
            )
            return if (isCorrect) {
                val nextBlockIdx = activeChallenge.currentBlockIndex + 1
                if (nextBlockIdx < activeChallenge.totalBlocks) {
                    val nextChallenge = CustodianCodeStrategy.createBlockChallenge(
                        fullCode20 = activeChallenge.fullCode20,
                        blockIndex = nextBlockIdx
                    )
                    current.copy(
                        activeChallenge = nextChallenge,
                        subMenuCursorIndex = 0,
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = "✓ Blocco ${nextBlockIdx}/5 OK (${nextBlockIdx * 4}/20 car.)"
                    )
                } else {
                    // Tutti i 20 caratteri verificati! Sblocca i permessi Custode per 5 minuti
                    repository.grantCustodianUnlock(durationMinutes = 5)
                    current.copy(
                        activeChallenge = null,
                        subMenuCursorIndex = 0,
                        isCustodianUnlocked = true,
                        remainingCustodianUnlockMinutes = repository.getRemainingCustodianUnlockMinutes(),
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = "🔑 Codice 20/20 OK! Modifiche Custode sbloccate (5m)"
                    )
                }
            } else {
                val resetChallenge = CustodianCodeStrategy.createBlockChallenge(
                    fullCode20 = activeChallenge.fullCode20,
                    blockIndex = 0
                )
                current.copy(
                    activeChallenge = resetChallenge,
                    subMenuCursorIndex = 0,
                    currentTime = updatedTime,
                    batteryBars = updatedBattery,
                    statusBanner = "✗ Blocco errato! Riparti dal Blocco 1/5 (0/20)"
                )
            }
        }

        // Caso C: Menu principale di "8. Sblocco SOS" (4 opzioni)
        return when (current.subMenuCursorIndex) {
            0 -> {
                // 1. Pausa SOS 5m (max 2/giorno) -> Richiede Enigma Matematico (US 4.1 + US 4.2)
                val usedToday = repository.getUsedEmergencyPausesToday()
                val maxDaily = repository.getMaxDailyEmergencyPauses()
                val pauseLeft = repository.getRemainingEmergencyPauseMinutes()
                if (pauseLeft > 0) {
                    current.copy(
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = "Pausa SOS già attiva ($pauseLeft min rimasti)"
                    )
                } else if (usedToday >= maxDaily) {
                    current.copy(
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = "⛔ Hai già usato $usedToday/$maxDaily Pause SOS oggi!"
                    )
                } else {
                    val mathChallenge = MathPuzzleStrategy.generateChallenge(step = 1, totalSteps = 2)
                    current.copy(
                        activeChallenge = mathChallenge,
                        subMenuCursorIndex = 0,
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = "US 4.2: Risolvi 2 calcoli con ▲/▼ e OK"
                    )
                }
            }

            1 -> {
                // 2. Sblocco Custode (20 Caratteri) -> Avvia verifica 5 blocchi da 4 caratteri (US 4.3)
                val code20 = repository.getOrCreateCustodianCode()
                val custodianChallenge = CustodianCodeStrategy.createBlockChallenge(
                    fullCode20 = code20,
                    blockIndex = 0
                )
                current.copy(
                    custodianCode20 = code20,
                    activeChallenge = custodianChallenge,
                    subMenuCursorIndex = 0,
                    currentTime = updatedTime,
                    batteryBars = updatedBattery,
                    statusBanner = "US 4.3: Seleziona il Blocco 1/5 del Codice 20 car."
                )
            }

            2 -> {
                // 3. Rigenera / Mostra Codice Custode a 20 caratteri
                if (!repository.isDetoxBlockingActive() || repository.isCustodianUnlocked()) {
                    val newCode = repository.regenerateCustodianCode()
                    current.copy(
                        custodianCode20 = newCode,
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = "Nuovo Codice 20 car.: $newCode"
                    )
                } else {
                    current.copy(
                        currentTime = updatedTime,
                        batteryBars = updatedBattery,
                        statusBanner = "Codice Custode: ${current.custodianCode20}"
                    )
                }
            }

            else -> {
                // 4. Termina subito Pausa SOS / Riattiva protezione Nokia
                repository.setDetoxBlockingActive(true)
                current.copy(
                    isDetoxActive = repository.isDetoxBlockingActive(),
                    remainingEmergencyPauseMinutes = repository.getRemainingEmergencyPauseMinutes(),
                    currentTime = updatedTime,
                    batteryBars = updatedBattery,
                    statusBanner = "🔒 Protezione Modalità NOKIA attiva!"
                )
            }
        }
    }

    private fun emergencyRowSubtitle(cursorIndex: Int, code20: String): String = when (cursorIndex) {
        0 -> "US 4.1 & 4.2: Risolvi 2 calcoli per 5m di pausa (max 2/gg)"
        1 -> "US 4.3: Verifica i 20 caratteri per modificare Whitelist/Orari"
        2 -> "Codice 20 car.: $code20"
        else -> "Riattiva subito il blocco del Portiere Nokia"
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
            description = "Pausa 5m (Enigma) e Codice Custode 20 car."
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
