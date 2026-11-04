package com.retrophonedetox.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrophonedetox.domain.model.NokiaKey
import com.retrophonedetox.ui.theme.NokiaColors

/**
 * US 1.1 & US 1.2 - Il Tastierino Fisico Nokia (occupa il 20% inferiore dello schermo).
 *
 * Ispirato al blocco di comando del Nokia 3110 / 3310:
 * - A sinistra: Tasto ROSSO / C (Indietro / Chiudi) e Tasto VERDE (Conferma / Chiama)
 * - Al centro: Il grande tasto "Navi-Key" blu/argento (OK / Seleziona)
 * - A destra: Il bilanciere direzionale (▲ SU e ▼ GIÙ) per scorrere il menu LCD
 *
 * Ogni pressione genera un feedback aptico (vibrazione secca del motore del telefono)
 * e abbassa visivamente il tasto.
 */
@Composable
fun NokiaKeypad(
    onKeyPress: (NokiaKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    // Funzione che attiva la vibrazione fisica e notifica il ViewModel
    val triggerKey: (NokiaKey) -> Unit = { key ->
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onKeyPress(key)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NokiaColors.BodyShellDark)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // COLONNA SINISTRA: Tasto Rosso (C / Indietro) + Tasto Verde (Chiama)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PhysicalNokiaButton(
                    label = "C",
                    subLabel = "━━",
                    accentColor = NokiaColors.BackRedAccent,
                    onClick = { triggerKey(NokiaKey.BACK_RED) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                PhysicalNokiaButton(
                    label = "✆",
                    subLabel = "━━",
                    accentColor = NokiaColors.CallGreenAccent,
                    onClick = { triggerKey(NokiaKey.CALL_GREEN) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            // COLONNA CENTRALE: Il grande tasto "Navi-Key" (OK / MENU) del Nokia 3310
            PhysicalNokiaButton(
                label = "OK",
                subLabel = "━━━",
                accentColor = NokiaColors.NaviBlueAccent,
                isPrimaryNaviKey = true,
                onClick = { triggerKey(NokiaKey.OK) },
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
            )

            // COLONNA DESTRA: Frecce direzionali (▲ SU / ▼ GIÙ)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PhysicalNokiaButton(
                    label = "▲",
                    subLabel = "SU",
                    accentColor = NokiaColors.KeyBorderDark,
                    onClick = { triggerKey(NokiaKey.UP) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                PhysicalNokiaButton(
                    label = "▼",
                    subLabel = "GIÙ",
                    accentColor = NokiaColors.KeyBorderDark,
                    onClick = { triggerKey(NokiaKey.DOWN) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Singolo tasto fisico sagomato con effetto di pressione e bordo metallico.
 */
@Composable
private fun PhysicalNokiaButton(
    label: String,
    subLabel: String,
    accentColor: Color,
    isPrimaryNaviKey: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shape = RoundedCornerShape(if (isPrimaryNaviKey) 22.dp else 14.dp)
    val backgroundColor = if (isPressed) NokiaColors.KeyCapPressed else NokiaColors.KeyCapSilver
    val borderWidth = if (isPressed) 2.dp else 3.dp

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, NokiaColors.KeyBorderDark, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = if (isPrimaryNaviKey) 24.sp else 18.sp,
                    fontWeight = FontWeight.Black
                ),
                color = NokiaColors.KeyBorderDark,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .height(4.dp)
                    .fillMaxWidth(if (isPrimaryNaviKey) 0.55f else 0.4f)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            if (!isPrimaryNaviKey && (subLabel == "SU" || subLabel == "GIÙ")) {
                Text(
                    text = subLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                    color = NokiaColors.KeyBorderDark
                )
            }
        }
    }
}
