package com.retrophonedetox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.retrophonedetox.domain.model.NokiaKey
import com.retrophonedetox.ui.screens.NokiaHomeScreen
import com.retrophonedetox.ui.theme.NokiaTheme
import com.retrophonedetox.ui.viewmodel.NokiaViewModel

/**
 * Punto di ingresso principale dell'app Android "Modalità NOKIA".
 *
 * Intercetta anche il tasto "Indietro" di sistema Android e lo trasforma
 * nella pressione del Tasto Rosso (C) del Nokia, evitando l'uscita accidentale.
 */
class MainActivity : ComponentActivity() {

    private val nokiaViewModel: NokiaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Intercetta il gesto/tasto "Indietro" di Android e lo collega al tasto ROSSO (C) del Nokia
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    nokiaViewModel.onKeyPress(NokiaKey.BACK_RED)
                }
            }
        )

        setContent {
            NokiaTheme {
                NokiaHomeScreen(viewModel = nokiaViewModel)
            }
        }
    }
}
