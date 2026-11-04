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
import com.retrophonedetox.domain.model.NokiaKey
import com.retrophonedetox.ui.components.NokiaKeypad
import com.retrophonedetox.ui.components.NokiaLcdDisplay
import com.retrophonedetox.ui.theme.Nokia5200ShellColors
import com.retrophonedetox.ui.theme.NokiaTheme
import com.retrophonedetox.ui.viewmodel.NokiaUiState
import com.retrophonedetox.ui.viewmodel.NokiaViewModel

/**
 * Schermata principale della Modalità NOKIA (US 1.1 & US 1.2).
 *
 * Divide verticalmente lo schermo dello smartphone in:
 * - 80% Superiore (`weight(0.8f)`): Bordino bianco con scritta NOKIA nera a destra + Display LCD a tutta larghezza.
 * - 20% Inferiore (`weight(0.2f)`): Tastierino avorio fedele agli anni 2000 con i soli trattini colorati e bottone OK centrale.
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
            selectedIndex = uiState.selectedIndex,
            currentTime = uiState.currentTime,
            batteryBars = uiState.batteryBars,
            signalBars = uiState.signalBars,
            isDetoxActive = uiState.isDetoxActive,
            openedItem = uiState.openedItem,
            themeCursorIndex = uiState.themeCursorIndex,
            statusBanner = uiState.statusBanner,
            palette = uiState.palette,
            modifier = Modifier.weight(0.8f)
        )

        // 20% INFERIORE: Tastierino Fisico anni 2000 con vibrazione
        NokiaKeypad(
            onKeyPress = onKeyPress,
            modifier = Modifier.weight(0.2f)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun NokiaHomeScreenPreview() {
    NokiaTheme {
        NokiaHomeContent(
            uiState = NokiaUiState(),
            onKeyPress = {}
        )
    }
}
