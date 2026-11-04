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
import androidx.compose.foundation.layout.size
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
import com.retrophonedetox.ui.theme.Nokia5200ShellColors

/**
 * US 1.1 & US 1.2 - Tastierino Fisico Nokia 5200 (20% inferiore):
 *
 * - A sinistra:
 *   1. Tasto Funzione Sinistro (`SOFT_LEFT`): trattino blu + simbolo minimale "≡" (Seleziona/Menu)
 *   2. Tasto Verde (`CALL_GREEN`): trattino verde + cornetta "✆" verde (Chiama subito)
 * - Al centro:
 *   - Anello quadrato arrotondato con freccette "▲" sopra e "▼" sotto + pulsante centrale quadrato "OK"
 * - A destra:
 *   1. Tasto Funzione Destro (`SOFT_RIGHT`): trattino blu + "C / ↩" (Indietro/Cancella)
 *   2. Tasto Rosso (`END_RED`): trattino rosso + simbolo uscita/fine chiamata "⏻" in rosso (Chiudi/Home)
 */
@Composable
fun NokiaKeypad(
    onKeyPress: (NokiaKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val triggerKey: (NokiaKey) -> Unit = { key ->
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onKeyPress(key)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Nokia5200ShellColors.ShellIvory)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp, topStart = 8.dp, topEnd = 8.dp))
                .background(Nokia5200ShellColors.ShellIvoryRecessed)
                .border(
                    width = 1.5.dp,
                    color = Nokia5200ShellColors.ShellBorderGray,
                    shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp, topStart = 8.dp, topEnd = 8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // COLONNA SINISTRA: Trattino Blu + "≡" (Soft-Key Sx) | Trattino Verde + "✆" (Chiamata)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Nokia5200MinimalSideKey(
                    barColor = Nokia5200ShellColors.SoftKeyBlueBar,
                    symbol = "≡",
                    symbolColor = Nokia5200ShellColors.KeyLabelDark,
                    onClick = { triggerKey(NokiaKey.SOFT_LEFT) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(Nokia5200ShellColors.ShellBorderGray)
                )
                Nokia5200MinimalSideKey(
                    barColor = Nokia5200ShellColors.CallGreenBar,
                    symbol = "✆",
                    symbolColor = Nokia5200ShellColors.CallGreenBar,
                    onClick = { triggerKey(NokiaKey.CALL_GREEN) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }

            // CENTRO: Anello Quadrato Arrotondato con ▲ / ▼ + Pulsante Centrale "OK"
            Nokia5200CenterSquircleRing(
                onUpClick = { triggerKey(NokiaKey.UP) },
                onOkClick = { triggerKey(NokiaKey.OK) },
                onDownClick = { triggerKey(NokiaKey.DOWN) },
                modifier = Modifier
                    .weight(1.35f)
                    .fillMaxHeight()
            )

            // COLONNA DESTRA: Trattino Blu + "C / ↩" (Soft-Key Dx) | Trattino Rosso + "⏻" (Chiudi/Home)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Nokia5200MinimalSideKey(
                    barColor = Nokia5200ShellColors.SoftKeyBlueBar,
                    symbol = "C / ↩",
                    symbolColor = Nokia5200ShellColors.KeyLabelDark,
                    onClick = { triggerKey(NokiaKey.SOFT_RIGHT) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(Nokia5200ShellColors.ShellBorderGray)
                )
                Nokia5200MinimalSideKey(
                    barColor = Nokia5200ShellColors.EndCallRedBar,
                    symbol = "⏻",
                    symbolColor = Nokia5200ShellColors.EndCallRedBar,
                    onClick = { triggerKey(NokiaKey.END_RED) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Tasto laterale minimale con trattino colorato + piccolo simbolo/lettera indicativa.
 */
@Composable
private fun Nokia5200MinimalSideKey(
    barColor: Color,
    symbol: String,
    symbolColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bg = if (isPressed) Nokia5200ShellColors.ShellIvoryPressed else Nokia5200ShellColors.ShellIvory

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 3.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.52f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barColor)
        )
        Text(
            text = symbol,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
            color = symbolColor,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

/**
 * Anello centrale quadrato arrotondato ("Squircle") con freccette ▲ / ▼ e pulsante centrale "OK".
 */
@Composable
private fun Nokia5200CenterSquircleRing(
    onUpClick: () -> Unit,
    onOkClick: () -> Unit,
    onDownClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outerShape = RoundedCornerShape(24.dp)
    val centerShape = RoundedCornerShape(12.dp)

    val upInteraction = remember { MutableInteractionSource() }
    val isUpPressed by upInteraction.collectIsPressedAsState()

    val downInteraction = remember { MutableInteractionSource() }
    val isDownPressed by downInteraction.collectIsPressedAsState()

    val okInteraction = remember { MutableInteractionSource() }
    val isOkPressed by okInteraction.collectIsPressedAsState()

    Box(
        modifier = modifier
            .clip(outerShape)
            .background(Nokia5200ShellColors.ShellIvory)
            .border(3.dp, Nokia5200ShellColors.ShellBorderGray, outerShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Metà superiore dell'anello con piccola freccetta ▲ sopra il tasto OK
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        if (isUpPressed) Nokia5200ShellColors.ShellIvoryPressed
                        else Color.Transparent
                    )
                    .clickable(
                        interactionSource = upInteraction,
                        indication = null,
                        onClick = onUpClick
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "▲",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Nokia5200ShellColors.KeyLabelDark,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            // Metà inferiore dell'anello con piccola freccetta ▼ sotto il tasto OK
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        if (isDownPressed) Nokia5200ShellColors.ShellIvoryPressed
                        else Color.Transparent
                    )
                    .clickable(
                        interactionSource = downInteraction,
                        indication = null,
                        onClick = onDownClick
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "▼",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Nokia5200ShellColors.KeyLabelDark,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }

        // Pulsante Quadrato Arrotondato Centrale con scritta "OK"
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 44.dp)
                .clip(centerShape)
                .background(
                    if (isOkPressed) Nokia5200ShellColors.ShellIvoryPressed
                    else Nokia5200ShellColors.ShellIvoryRecessed
                )
                .border(2.dp, Nokia5200ShellColors.ShellBorderGray, centerShape)
                .clickable(
                    interactionSource = okInteraction,
                    indication = null,
                    onClick = onOkClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "OK",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                fontWeight = FontWeight.Black,
                color = Nokia5200ShellColors.KeyLabelDark
            )
        }
    }
}
