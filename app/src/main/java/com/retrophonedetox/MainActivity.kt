package com.retrophonedetox

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.retrophonedetox.domain.model.NokiaKey
import com.retrophonedetox.service.DetoxGuardService
import com.retrophonedetox.ui.screens.NokiaHomeScreen
import com.retrophonedetox.ui.theme.NokiaTheme
import com.retrophonedetox.ui.viewmodel.NokiaViewModel

/**
 * Punto di ingresso principale dell'app Android "Modalità NOKIA".
 *
 * Riceve anche i reindirizzamenti istantanei dal "Portiere" (`DetoxGuardService`)
 * quando un'app fuori dalla Whitelist o le Impostazioni vengono bloccate (US 2.1 & US 2.2).
 */
class MainActivity : ComponentActivity() {

    private val nokiaViewModel: NokiaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingGuardIntent(intent)

        // Il tasto Indietro di Android agisce come il tasto "Indietro" del Nokia (non fa uscire dall'app!)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingGuardIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        nokiaViewModel.refreshSystemStatus()
    }

    private fun handleIncomingGuardIntent(incomingIntent: Intent?) {
        val blockedBanner = incomingIntent?.getStringExtra(DetoxGuardService.EXTRA_BLOCKED_BANNER)
        if (!blockedBanner.isNullOrBlank()) {
            nokiaViewModel.refreshSystemStatus(blockedBanner = "⛔ $blockedBanner")
            incomingIntent.removeExtra(DetoxGuardService.EXTRA_BLOCKED_BANNER)
        }
    }
}
