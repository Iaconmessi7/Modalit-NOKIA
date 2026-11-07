package com.retrophonedetox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retrophonedetox.domain.model.AllowedApp
import com.retrophonedetox.domain.model.NokiaKey
import com.retrophonedetox.ui.components.NokiaKeypad
import com.retrophonedetox.ui.components.NokiaLcdDisplay
import com.retrophonedetox.ui.theme.Nokia5200ShellColors
import com.retrophonedetox.ui.theme.NokiaTheme
import com.retrophonedetox.ui.viewmodel.NokiaUiState
import com.retrophonedetox.ui.viewmodel.NokiaViewModel
import com.retrophonedetox.ui.viewmodel.buildNokiaMenuFromWhitelist

/**
 * Schermata principale della Modalità NOKIA (Sprint 1: US 1.1, 1.2, 1.3, 1.4).
 */
@Composable
fun NokiaHomeScreen(
    viewModel: NokiaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    NokiaHomeContent(
        uiState = uiState,
        onKeyPress = viewModel::onKeyPress
    )
}

@Composable
fun NokiaHomeContent(
    uiState: NokiaUiState,
    onKeyPress: (NokiaKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Nokia5200ShellColors.ShellIvory)
            .safeDrawingPadding()
    ) {
        // 80% SUPERIORE: Display LCD a tutta larghezza + Bordino superiore con logo NOKIA
        NokiaLcdDisplay(
            menuItems = uiState.menuItems,
            configurableApps = uiState.configurableApps,
            selectedIndex = uiState.selectedIndex,
            currentTime = uiState.currentTime,
            batteryBars = uiState.batteryBars,
            signalBars = uiState.signalBars,
            isDetoxActive = uiState.isDetoxActive,
            openedItem = uiState.openedItem,
            subMenuCursorIndex = uiState.subMenuCursorIndex,
            statusBanner = uiState.statusBanner,
            palette = uiState.palette,
            modifier = Modifier.weight(0.8f)
        )

        // 20% INFERIORE: Tastierino Fisico Ergonomico a 5 tasti con vibrazione
        NokiaKeypad(
            onKeyPress = onKeyPress,
            modifier = Modifier.weight(0.2f)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun NokiaHomeScreenPreview() {
    val sampleApps = listOf(
        AllowedApp("phone", "Telefono", "com.android.dialer", "Apri Telefono", true, true),
        AllowedApp("messages", "Messaggi SMS", "com.google.android.apps.messaging", "Apri SMS", true, true),
        AllowedApp("whatsapp", "WhatsApp", "com.whatsapp", "Apri WhatsApp", false, true)
    )
    NokiaTheme {
        NokiaHomeContent(
            uiState = NokiaUiState(
                configurableApps = sampleApps,
                menuItems = buildNokiaMenuFromWhitelist(sampleApps)
            ),
            onKeyPress = {}
        )
    }
}
