package com.retrophonedetox.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Palette cromatica ispirata ai classici Nokia 3110 / 3310:
 * - Retroilluminazione verde oliva LCD ("Nokia Green")
 * - Inchiostro a cristalli liquidi nero/verde scuro ad alto contrasto
 * - Scocca blu-grigio scuro ("Nokia Navy/Charcoal") e tasti argentati
 */
object NokiaColors {
    // Colori del Display LCD (80% superiore)
    val LcdBackground = Color(0xFF9BBC0F)       // Verde oliva classico retroilluminato
    val LcdBackgroundShadow = Color(0xFF8BAC0F) // Ombra matrice LCD / pixel spenti
    val LcdInk = Color(0xFF0F380F)              // Cristalli liquidi attivi (nero-verde scuro)
    val LcdInkMuted = Color(0xFF306230)         // Mezzo tono per bordi secondari

    // Colori della Scocca e del Tastierino (20% inferiore)
    val BodyShellDark = Color(0xFF1E242B)       // Scocca esterna antracite/blu Nokia
    val BezelSilver = Color(0xFF8C969E)         // Cornice metallica attorno al display
    val KeyCapSilver = Color(0xFFD6DCE2)        // Tasto fisico argento chiaro
    val KeyCapPressed = Color(0xFFA5B0BA)       // Tasto fisico premuto
    val KeyBorderDark = Color(0xFF111519)       // Bordo incassato dei tasti
    val CallGreenAccent = Color(0xFF1B8A3D)     // Simbolo cornetta verde
    val BackRedAccent = Color(0xFFC92A2A)       // Simbolo cornetta rossa / C
    val NaviBlueAccent = Color(0xFF1B4965)      // Dettaglio centrale Navi-Key
}

/**
 * Tipografia Monospace ad alto contrasto per simulare i caratteri bitmap del Nokia.
 */
val NokiaTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp,
        letterSpacing = 1.5.sp,
        color = NokiaColors.LcdInk
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 1.sp,
        color = NokiaColors.LcdInk
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = 0.8.sp,
        color = NokiaColors.LcdInk
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.6.sp,
        color = NokiaColors.LcdInk
    )
)

private val NokiaColorScheme = darkColorScheme(
    primary = NokiaColors.LcdBackground,
    onPrimary = NokiaColors.LcdInk,
    background = NokiaColors.BodyShellDark,
    surface = NokiaColors.BodyShellDark,
    onBackground = NokiaColors.LcdBackground,
    onSurface = NokiaColors.LcdBackground
)

@Composable
fun NokiaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NokiaColorScheme,
        typography = NokiaTypography,
        content = content
    )
}
