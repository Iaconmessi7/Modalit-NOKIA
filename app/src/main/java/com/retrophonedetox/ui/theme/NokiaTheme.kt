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
 * I 4 Temi Anti-Luce Blu selezionabili per il display LCD del Nokia.
 * Progettati con toni caldi, opachi e ad alto contrasto per non affaticare la vista.
 */
enum class NokiaLcdPalette(
    val displayName: String,
    val lcdBackground: Color,
    val lcdShadow: Color,
    val lcdInk: Color
) {
    /** 1. Salvia LCD (Default): verde-grigio salvia caldo e opaco con pixel carbone */
    SAGE_LCD(
        displayName = "Salvia LCD",
        lcdBackground = Color(0xFF8F9779),
        lcdShadow = Color(0xFF81896B),
        lcdInk = Color(0xFF1F241B)
    ),

    /** 2. Bianco e Nero (E-Ink): carta calda opaca a zero luce blu con inchiostro nero */
    E_INK_BW(
        displayName = "Bianco e Nero (E-Ink)",
        lcdBackground = Color(0xFFEAE6DF),
        lcdShadow = Color(0xFFDCD7CE),
        lcdInk = Color(0xFF1A1A1A)
    ),

    /** 3. Ambra Seppia: tono sabbia/ambra calda con pixel marrone espresso scuro */
    AMBER_SEPIA(
        displayName = "Ambra Seppia",
        lcdBackground = Color(0xFFC8B898),
        lcdShadow = Color(0xFFB9A888),
        lcdInk = Color(0xFF261E16)
    ),

    /** 4. OLED Notte: sfondo nero profondo con pixel ambra morbido (Super Risparmio Energetico) */
    OLED_NIGHT(
        displayName = "OLED Notte",
        lcdBackground = Color(0xFF141210),
        lcdShadow = Color(0xFF1F1B17),
        lcdInk = Color(0xFFD99B4E)
    );

    /** Passa ciclicamente al tema successivo */
    fun next(): NokiaLcdPalette {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }
}

/**
 * Colori fisici della scocca ispirata al Nokia 5200 XpressMusic (Bianco Avorio + Bande Rosse + Cornice Nera).
 */
object Nokia5200ShellColors {
    val ShellIvory = Color(0xFFF2EFE9)          // Scocca bianco perla / avorio caldo
    val ShellIvoryRecessed = Color(0xFFE4E0D8)  // Superficie incassata tastierino
    val ShellIvoryPressed = Color(0xFFD2CDC3)   // Tasto avorio premuto
    val ShellBorderGray = Color(0xFF9E9990)     // Fessure/bordi tra i tasti e anello Navi-Key
    val SideBandRed = Color(0xFFC83E3B)         // Banda laterale rossa XpressMusic
    val SideBandRedDark = Color(0xFF9E2B28)     // Bordo scuro banda rossa
    val ScreenBezelBlack = Color(0xFF141414)    // Cornice nera attorno al display
    val SoftKeyBlueBar = Color(0xFF2B5B84)      // Trattino blu tasti funzione superiori
    val CallGreenBar = Color(0xFF1E7A3B)        // Trattino verde tasto chiamata (in basso a sinistra)
    val EndCallRedBar = Color(0xFFB83330)       // Trattino rosso tasto fine chiamata (in basso a destra)
    val KeyLabelDark = Color(0xFF2C2A29)        // Colore icone/testi sui tasti avorio
}

/**
 * Tipografia Monospace ad alto contrasto per simulare i caratteri bitmap del Nokia.
 */
val NokiaTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Black,
        fontSize = 26.sp,
        letterSpacing = 1.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        letterSpacing = 0.8.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.6.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    )
)

private val NokiaColorScheme = darkColorScheme(
    primary = NokiaLcdPalette.SAGE_LCD.lcdBackground,
    onPrimary = NokiaLcdPalette.SAGE_LCD.lcdInk,
    background = Nokia5200ShellColors.ShellIvory,
    surface = Nokia5200ShellColors.ShellIvory,
    onBackground = Nokia5200ShellColors.KeyLabelDark,
    onSurface = Nokia5200ShellColors.KeyLabelDark
)

@Composable
fun NokiaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NokiaColorScheme,
        typography = NokiaTypography,
        content = content
    )
}
