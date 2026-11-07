package com.retrophonedetox.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrophonedetox.domain.model.AllowedApp
import com.retrophonedetox.domain.model.ConnectionOption
import com.retrophonedetox.domain.model.DetoxSchedule
import com.retrophonedetox.domain.model.NokiaMenuItem
import com.retrophonedetox.domain.model.QuickTimerPreset
import com.retrophonedetox.domain.strategy.UnlockChallenge
import com.retrophonedetox.ui.theme.Nokia5200ShellColors
import com.retrophonedetox.ui.theme.NokiaLcdPalette
import com.retrophonedetox.ui.viewmodel.NokiaViewModel

/**
 * Sezione Superiore (80%) della Modalità NOKIA (Sprint 1 + Sprint 2 + Sprint 3 + Sprint 4):
 * Include anche "Sblocco SOS" (US 4.1 Pausa 5m max 2/gg, US 4.2 Enigma Matematico, US 4.3 Codice Custode 20 caratteri).
 */
@Composable
fun NokiaLcdDisplay(
    menuItems: List<NokiaMenuItem>,
    configurableApps: List<AllowedApp>,
    schedules: List<DetoxSchedule>,
    remainingQuickTimerMinutes: Int,
    usedEmergencyPausesToday: Int,
    maxDailyEmergencyPauses: Int,
    remainingEmergencyPauseMinutes: Int,
    custodianCode20: String,
    isCustodianUnlocked: Boolean,
    remainingCustodianUnlockMinutes: Int,
    activeChallenge: UnlockChallenge?,
    showOnboardingWizard: Boolean,
    onboardingCursorIndex: Int,
    selectedIndex: Int,
    currentTime: String,
    batteryBars: Int,
    signalBars: Int,
    isDetoxActive: Boolean,
    isGuardPermissionEnabled: Boolean,
    blockedAttemptsCount: Int,
    lastBlockedApp: String?,
    openedItem: NokiaMenuItem?,
    subMenuCursorIndex: Int,
    statusBanner: String?,
    palette: NokiaLcdPalette,
    modifier: Modifier = Modifier
) {
    val mainListState = rememberLazyListState()
    val whitelistListState = rememberLazyListState()
    val scheduleListState = rememberLazyListState()

    LaunchedEffect(selectedIndex, openedItem) {
        if (openedItem == null && menuItems.isNotEmpty() && selectedIndex in menuItems.indices) {
            mainListState.animateScrollToItem(selectedIndex)
        }
    }

    LaunchedEffect(subMenuCursorIndex, openedItem) {
        if (openedItem?.id == NokiaViewModel.MENU_ID_WHITELIST &&
            configurableApps.isNotEmpty() &&
            subMenuCursorIndex in configurableApps.indices
        ) {
            whitelistListState.animateScrollToItem(subMenuCursorIndex)
        } else if (openedItem?.id == NokiaViewModel.MENU_ID_SCHEDULE) {
            val totalRows = QuickTimerPreset.entries.size + schedules.size
            if (totalRows > 0 && subMenuCursorIndex in 0 until totalRows) {
                scheduleListState.animateScrollToItem(subMenuCursorIndex)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Nokia5200ShellColors.ShellIvory)
    ) {
        NokiaTopIvoryHeaderBar()

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Nokia5200ShellColors.ScreenBezelBlack)
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(6.dp))
                    .background(palette.lcdBackground)
                    .border(2.dp, palette.lcdInk, RoundedCornerShape(6.dp))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                event.changes.forEach { it.consume() }
                            }
                        }
                    }
            ) {
                LcdPixelMatrixOverlay(shadowColor = palette.lcdShadow)

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NokiaVerticalBarMeter(
                        activeBars = signalBars,
                        maxBars = 4,
                        bottomSymbol = "Ψ",
                        inkColor = palette.lcdInk,
                        shadowColor = palette.lcdShadow,
                        modifier = Modifier.fillMaxHeight()
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        val currentMenuNumber = if (openedItem != null) {
                            "${openedItem.indexNumber}-${subMenuCursorIndex + 1}"
                        } else {
                            "1-${(selectedIndex + 1).coerceAtMost(menuItems.size.coerceAtLeast(1))}"
                        }

                        NokiaTopHeader(
                            detoxActive = isDetoxActive,
                            remainingTimerMinutes = remainingQuickTimerMinutes,
                            remainingEmergencyPauseMinutes = remainingEmergencyPauseMinutes,
                            isCustodianUnlocked = isCustodianUnlocked,
                            remainingCustodianUnlockMinutes = remainingCustodianUnlockMinutes,
                            menuIndexText = currentMenuNumber,
                            timeText = currentTime,
                            inkColor = palette.lcdInk
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            if (showOnboardingWizard) {
                                // US 5.1: Wizard Permessi Iniziale alla prima apertura
                                NokiaOnboardingWizardView(
                                    isGuardPermissionEnabled = isGuardPermissionEnabled,
                                    custodianCode20 = custodianCode20,
                                    palette = palette,
                                    selectedRowIndex = onboardingCursorIndex
                                )
                            } else when (openedItem?.id) {
                                // US 4.1, US 4.2 & US 4.3: Sottomenu "Sblocco SOS"
                                NokiaViewModel.MENU_ID_EMERGENCY -> {
                                    NokiaEmergencySubMenu(
                                        usedEmergencyPausesToday = usedEmergencyPausesToday,
                                        maxDailyEmergencyPauses = maxDailyEmergencyPauses,
                                        remainingEmergencyPauseMinutes = remainingEmergencyPauseMinutes,
                                        custodianCode20 = custodianCode20,
                                        isCustodianUnlocked = isCustodianUnlocked,
                                        activeChallenge = activeChallenge,
                                        selectedOptionIndex = subMenuCursorIndex,
                                        palette = palette
                                    )
                                }

                                // US 3.1 & US 3.2: Sottomenu "Orari Detox" (Timer Rapido + Fasce Orarie Sveglia)
                                NokiaViewModel.MENU_ID_SCHEDULE -> {
                                    NokiaScheduleSubMenu(
                                        schedules = schedules,
                                        remainingQuickTimerMinutes = remainingQuickTimerMinutes,
                                        selectedRowIndex = subMenuCursorIndex,
                                        listState = scheduleListState,
                                        palette = palette
                                    )
                                }

                                // US 2.1 & US 2.2: Sottomenu "Portiere Detox"
                                NokiaViewModel.MENU_ID_GUARD -> {
                                    NokiaGuardStatusSubMenu(
                                        selectedOptionIndex = subMenuCursorIndex,
                                        isDetoxActive = isDetoxActive,
                                        isGuardPermissionEnabled = isGuardPermissionEnabled,
                                        blockedAttemptsCount = blockedAttemptsCount,
                                        lastBlockedApp = lastBlockedApp,
                                        palette = palette
                                    )
                                }

                                // US 1.4: Sottomenu "Connessioni"
                                NokiaViewModel.MENU_ID_CONNECTIONS -> {
                                    NokiaConnectionsSubMenu(
                                        selectedOptionIndex = subMenuCursorIndex,
                                        palette = palette
                                    )
                                }

                                // US 1.3: Sottomenu "App Consentite"
                                NokiaViewModel.MENU_ID_WHITELIST -> {
                                    NokiaWhitelistSubMenu(
                                        apps = configurableApps,
                                        selectedAppIndex = subMenuCursorIndex,
                                        listState = whitelistListState,
                                        palette = palette
                                    )
                                }

                                // Sottomenu "Tema Display"
                                NokiaViewModel.MENU_ID_THEME -> {
                                    NokiaThemeSelectorSubMenu(
                                        selectedThemeIndex = subMenuCursorIndex,
                                        activePalette = palette
                                    )
                                }

                                null -> {
                                    LazyColumn(
                                        state = mainListState,
                                        userScrollEnabled = false,
                                        verticalArrangement = Arrangement.spacedBy(5.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        itemsIndexed(menuItems) { index, item ->
                                            val suffix = if (item.packageName != null) " ↗" else ""
                                            NokiaMenuRow(
                                                text = "${item.indexNumber}. ${item.title}$suffix",
                                                isSelected = (index == selectedIndex),
                                                palette = palette
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    NokiaOpenedItemView(
                                        item = openedItem,
                                        statusBanner = statusBanner,
                                        inkColor = palette.lcdInk
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val selectedItem = menuItems.getOrNull(selectedIndex)
                        NokiaBottomSoftKeyBar(
                            subtitle = statusBanner ?: selectedItem?.description.orEmpty(),
                            leftSoftKey = "Home / Indietro",
                            rightSoftKey = "▲ Su / Giù ▼",
                            inkColor = palette.lcdInk
                        )
                    }

                    NokiaVerticalBarMeter(
                        activeBars = batteryBars,
                        maxBars = 4,
                        bottomSymbol = "▣",
                        inkColor = palette.lcdInk,
                        shadowColor = palette.lcdShadow,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
        }
    }
}

/**
 * US 4.1, US 4.2 & US 4.3 - Sottomenu "8. Sblocco SOS":
 * - Mostra le 4 opzioni di sicurezza (Pausa 5m con Enigma, Sblocco Custode 20 car., Visualizza/Rigenera Codice, Riattiva Blocco)
 * - Oppure mostra la schermata interattiva della sfida (`MathPuzzleChallenge` o `CustodianCodeChallenge`).
 */
@Composable
private fun NokiaEmergencySubMenu(
    usedEmergencyPausesToday: Int,
    maxDailyEmergencyPauses: Int,
    remainingEmergencyPauseMinutes: Int,
    custodianCode20: String,
    isCustodianUnlocked: Boolean,
    activeChallenge: UnlockChallenge?,
    selectedOptionIndex: Int,
    palette: NokiaLcdPalette
) {
    if (activeChallenge != null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = activeChallenge.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.lcdInk,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = activeChallenge.progressLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.lcdInk,
                    fontWeight = FontWeight.Black
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, palette.lcdInk)
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = activeChallenge.promptLine1,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                    color = palette.lcdInk,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = activeChallenge.promptLine2,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp),
                    color = palette.lcdInk,
                    fontWeight = FontWeight.Black
                )
            }
            activeChallenge.options.forEachIndexed { idx, optionText ->
                NokiaMenuRow(
                    text = optionText,
                    isSelected = (idx == selectedOptionIndex),
                    palette = palette
                )
            }
        }
    } else {
        val pauseLabel = if (remainingEmergencyPauseMinutes > 0) {
            "1. Pausa SOS: ATTIVA (${remainingEmergencyPauseMinutes}m)"
        } else {
            "1. Pausa SOS 5m ($usedEmergencyPausesToday/$maxDailyEmergencyPauses oggi) 🧩"
        }
        val custodianLabel = if (isCustodianUnlocked) {
            "2. Custode: SBLOCCATO ✓"
        } else {
            "2. Sblocco Custode (20 Car.) 🔑"
        }
        val shortCodePreview = custodianCode20.take(9) + "..."
        val rows = listOf(
            pauseLabel,
            custodianLabel,
            "3. Codice: $shortCodePreview 📜",
            "4. Riattiva Blocco Nokia 🔒"
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "SBLOCCO SOS & CUSTODE (EPIC 4):",
                style = MaterialTheme.typography.labelMedium,
                color = palette.lcdInk,
                fontWeight = FontWeight.Black
            )
            rows.forEachIndexed { idx, rowText ->
                NokiaMenuRow(
                    text = rowText,
                    isSelected = (idx == selectedOptionIndex),
                    palette = palette
                )
            }
        }
    }
}

/**
 * US 3.1 & US 3.2 - Sottomenu "Orari Detox":
 * - Righe 1..3: Avvio rapido manuale a tempo (30 min, 1 ora, 2 ore)
 * - Righe 4..7: Fasce orarie ricorrenti stile Sveglia ([✓] / [ ]) gestite da AlarmManager
 */
@Composable
private fun NokiaScheduleSubMenu(
    schedules: List<DetoxSchedule>,
    remainingQuickTimerMinutes: Int,
    selectedRowIndex: Int,
    listState: androidx.compose.foundation.lazy.LazyListState,
    palette: NokiaLcdPalette
) {
    val quickPresets = QuickTimerPreset.entries
    val allRows = buildList {
        quickPresets.forEach { preset ->
            add("▶ ${preset.label}")
        }
        schedules.forEach { schedule ->
            val mark = if (schedule.isEnabled) "[✓]" else "[ ]"
            add("$mark ${schedule.title} (${schedule.daysTag})")
        }
    }

    val headerText = if (remainingQuickTimerMinutes > 0) {
        "ORARI DETOX (Timer: ${remainingQuickTimerMinutes}m):"
    } else {
        "TIMER RAPIDO & SVEGLIE DETOX:"
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = headerText,
            style = MaterialTheme.typography.labelMedium,
            color = palette.lcdInk,
            fontWeight = FontWeight.Black
        )
        LazyColumn(
            state = listState,
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(allRows) { idx, rowLabel ->
                NokiaMenuRow(
                    text = rowLabel,
                    isSelected = (idx == selectedRowIndex),
                    palette = palette
                )
            }
        }
    }
}

/**
 * US 5.1 - Wizard Permessi Iniziale alla prima apertura:
 * Guida l'utente con i 5 tasti fisici ad abilitare il permesso del "Portiere",
 * annotare il Codice Custode di 20 caratteri e scegliere il Tema Anti-Luce Blu.
 */
@Composable
private fun NokiaOnboardingWizardView(
    isGuardPermissionEnabled: Boolean,
    custodianCode20: String,
    palette: NokiaLcdPalette,
    selectedRowIndex: Int
) {
    val rows = listOf(
        "1. Portiere: ${if (isGuardPermissionEnabled) "ATTIVO ✓" else "ABILITA ⚙"}",
        "2. Codice 20c: ${custodianCode20.take(9)}...",
        "3. Tema: ${palette.displayName}",
        "4. ▶ ENTRA NEL NOKIA ✓"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "WIZARD BENVENUTO NOKIA (US 5.1):",
            style = MaterialTheme.typography.labelMedium,
            color = palette.lcdInk,
            fontWeight = FontWeight.Black
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, palette.lcdInk)
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = " Annota Codice Custode (20 car.):",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                color = palette.lcdInk,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = custodianCode20,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = palette.lcdInk,
                fontWeight = FontWeight.Black
            )
        }
        rows.forEachIndexed { idx, rowText ->
            NokiaMenuRow(
                text = rowText,
                isSelected = (idx == selectedRowIndex),
                palette = palette
            )
        }
    }
}

@Composable
private fun NokiaGuardStatusSubMenu(
    selectedOptionIndex: Int,
    isDetoxActive: Boolean,
    isGuardPermissionEnabled: Boolean,
    blockedAttemptsCount: Int,
    lastBlockedApp: String?,
    palette: NokiaLcdPalette
) {
    val rows = listOf(
        "1. Blocco: ${if (isDetoxActive) "ATTIVO 🔒" else "PAUSA 🔓"}",
        "2. Permesso: ${if (isGuardPermissionEnabled) "ATTIVO ✓" else "ABILITA ⚙"}",
        "3. Bloccate: $blockedAttemptsCount (${lastBlockedApp ?: "Nessuna"})",
        "4. Wizard Permessi Iniziale 🧭"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "PORTIERE DETOX (US 2.1 & 2.2):",
            style = MaterialTheme.typography.labelMedium,
            color = palette.lcdInk,
            fontWeight = FontWeight.Black
        )
        rows.forEachIndexed { idx, rowText ->
            NokiaMenuRow(
                text = rowText,
                isSelected = (idx == selectedOptionIndex),
                palette = palette
            )
        }
    }
}

@Composable
private fun NokiaTopIvoryHeaderBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Nokia5200ShellColors.ShellIvory)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(34.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Nokia5200ShellColors.ScreenBezelBlack)
            )
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Nokia5200ShellColors.ScreenBezelBlack)
            )
        }

        Text(
            text = "NOKIA",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            letterSpacing = 1.5.sp,
            color = Nokia5200ShellColors.ScreenBezelBlack,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun LcdPixelMatrixOverlay(shadowColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stepPx = 4.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = shadowColor.copy(alpha = 0.45f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
            y += stepPx
        }
    }
}

@Composable
private fun NokiaTopHeader(
    detoxActive: Boolean,
    remainingTimerMinutes: Int,
    remainingEmergencyPauseMinutes: Int,
    isCustodianUnlocked: Boolean,
    remainingCustodianUnlockMinutes: Int,
    menuIndexText: String,
    timeText: String,
    inkColor: Color
) {
    val leftTitle = when {
        remainingEmergencyPauseMinutes > 0 -> "🔓 PAUSA SOS (${remainingEmergencyPauseMinutes}m)"
        isCustodianUnlocked -> "🔑 CUSTODE (${remainingCustodianUnlockMinutes}m)"
        remainingTimerMinutes > 0 -> "🔒 NOKIA (⏱${remainingTimerMinutes}m)"
        detoxActive -> "🔒 MODALITÀ NOKIA"
        else -> "🔓 NOKIA (PAUSA)"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = leftTitle,
                style = MaterialTheme.typography.labelMedium,
                color = inkColor,
                fontWeight = FontWeight.Black
            )
            Text(
                text = menuIndexText,
                style = MaterialTheme.typography.labelMedium,
                color = inkColor,
                fontWeight = FontWeight.Black
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(inkColor)
            )
            Text(
                text = " $timeText ",
                style = MaterialTheme.typography.labelMedium,
                color = inkColor,
                fontWeight = FontWeight.Black
            )
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(inkColor)
            )
        }
    }
}

@Composable
private fun NokiaMenuRow(
    text: String,
    isSelected: Boolean,
    palette: NokiaLcdPalette
) {
    val backgroundColor = if (isSelected) palette.lcdInk else palette.lcdBackground
    val contentColor = if (isSelected) palette.lcdBackground else palette.lcdInk

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = palette.lcdInk
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isSelected) "▶" else " ",
            style = MaterialTheme.typography.titleLarge,
            color = contentColor,
            modifier = Modifier.width(22.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NokiaConnectionsSubMenu(
    selectedOptionIndex: Int,
    palette: NokiaLcdPalette
) {
    val options = ConnectionOption.entries

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "PANNELLO CONNESSIONI (OK=Apri):",
            style = MaterialTheme.typography.labelMedium,
            color = palette.lcdInk,
            fontWeight = FontWeight.Black
        )
        options.forEachIndexed { idx, opt ->
            NokiaMenuRow(
                text = "${opt.indexNumber}. ${opt.title} ⚙",
                isSelected = (idx == selectedOptionIndex),
                palette = palette
            )
        }
    }
}

@Composable
private fun NokiaWhitelistSubMenu(
    apps: List<AllowedApp>,
    selectedAppIndex: Int,
    listState: androidx.compose.foundation.lazy.LazyListState,
    palette: NokiaLcdPalette
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "APP NEL MENU NOKIA (OK=Cambia):",
            style = MaterialTheme.typography.labelMedium,
            color = palette.lcdInk,
            fontWeight = FontWeight.Black
        )
        LazyColumn(
            state = listState,
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(apps) { idx, app ->
                val mark = if (app.isEnabled) "[✓]" else "[ ]"
                val lockTag = if (app.isEssential) " *" else ""
                NokiaMenuRow(
                    text = "$mark ${app.title}$lockTag",
                    isSelected = (idx == selectedAppIndex),
                    palette = palette
                )
            }
        }
    }
}

@Composable
private fun NokiaThemeSelectorSubMenu(
    selectedThemeIndex: Int,
    activePalette: NokiaLcdPalette
) {
    val themes = NokiaLcdPalette.entries

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "SCEGLI TEMA DISPLAY:",
            style = MaterialTheme.typography.labelMedium,
            color = activePalette.lcdInk,
            fontWeight = FontWeight.Black
        )
        themes.forEachIndexed { idx, themeOption ->
            val checkMark = if (themeOption == activePalette) "✓" else " "
            NokiaMenuRow(
                text = "$checkMark ${idx + 1}. ${themeOption.displayName}",
                isSelected = (idx == selectedThemeIndex),
                palette = activePalette
            )
        }
    }
}

@Composable
private fun NokiaOpenedItemView(
    item: NokiaMenuItem,
    statusBanner: String?,
    inkColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(2.dp, inkColor)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "[ ${item.title.uppercase()} ]",
            style = MaterialTheme.typography.titleLarge,
            color = inkColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = statusBanner ?: item.description,
            style = MaterialTheme.typography.bodyLarge,
            color = inkColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Premi C / ↩ (basso a sx)\no ≡ (alto a sx) per la Home",
            style = MaterialTheme.typography.labelMedium,
            color = inkColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NokiaBottomSoftKeyBar(
    subtitle: String,
    leftSoftKey: String,
    rightSoftKey: String,
    inkColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            color = inkColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(inkColor)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = leftSoftKey,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                color = inkColor,
                fontWeight = FontWeight.Black
            )
            Text(
                text = rightSoftKey,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                color = inkColor,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun NokiaVerticalBarMeter(
    activeBars: Int,
    maxBars: Int,
    bottomSymbol: String,
    inkColor: Color,
    shadowColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (barLevel in maxBars downTo 1) {
                val isFilled = barLevel <= activeBars
                Box(
                    modifier = Modifier
                        .width(9.dp)
                        .height(16.dp)
                        .background(if (isFilled) inkColor else shadowColor)
                        .border(1.dp, inkColor)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = bottomSymbol,
            style = MaterialTheme.typography.labelMedium,
            color = inkColor,
            fontWeight = FontWeight.Black
        )
    }
}
