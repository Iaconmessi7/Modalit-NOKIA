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
import com.retrophonedetox.domain.model.NokiaMenuItem
import com.retrophonedetox.ui.theme.Nokia5200ShellColors
import com.retrophonedetox.ui.theme.NokiaLcdPalette
import com.retrophonedetox.ui.viewmodel.NokiaViewModel

/**
 * Sezione Superiore (80%) della Modalità NOKIA (Sprint 1 + Sprint 2):
 * Include anche la schermata del "Portiere Detox" (US 2.1 & US 2.2).
 */
@Composable
fun NokiaLcdDisplay(
    menuItems: List<NokiaMenuItem>,
    configurableApps: List<AllowedApp>,
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
                            when (openedItem?.id) {
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
 * US 2.1 & US 2.2 - Sottomenu "Portiere Detox" (`DetoxGuardService`):
 * Mostra lo stato del blocco, il permesso AccessibilityService e i tentativi intercettati.
 */
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
        "3. Bloccate: $blockedAttemptsCount (${lastBlockedApp ?: "Nessuna"})"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
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
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "✓ Blocca App fuori Whitelist e Home\n✓ Blocca Impostazioni e Notifiche",
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
            color = palette.lcdInk
        )
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
    menuIndexText: String,
    timeText: String,
    inkColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (detoxActive) "🔒 MODALITÀ NOKIA" else "🔓 NOKIA (PAUSA)",
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
