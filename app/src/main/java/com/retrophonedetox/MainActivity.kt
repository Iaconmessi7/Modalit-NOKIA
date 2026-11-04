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
 */
class MainActivity : ComponentActivity() {

    private val nokiaViewModel: NokiaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Il tasto Indietro di Android agisce come il Tasto Funzione Destro ("Indietro")
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    nokiaViewModel.onKeyPress(NokiaKey.SOFT_RIGHT)
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
