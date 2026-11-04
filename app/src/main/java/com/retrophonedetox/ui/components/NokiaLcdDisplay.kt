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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrophonedetox.domain.model.NokiaMenuItem
import com.retrophonedetox.ui.theme.NokiaColors

/**
 * US 1.1 & US 1.2 - Il Display LCD Monocromatico del Nokia (occupa l'80% superiore dello schermo).
 *
 * Caratteristiche:
 * 1. Effetto griglia a cristalli liquidi disegnato tramite Canvas.
 * 2. Indicatori laterali a blocchetti stile Nokia 3110/3310 (Segnale a sinistra, Batteria a destra).
 * 3. Intestazione con Operatore ("MODALITA NOKIA"), Orologio digitale e numero indice del menu (es. "1-6").
 * 4. Lista delle voci di menu con inversione cromatica LCD sulla riga selezionata.
 * 5. Touch completamente disabilitato sul display (`consumeAllTouchEvents`): si naviga SOLO col tastierino!
 */
@Composable
fun NokiaLcdDisplay(
    menuItems: List<NokiaMenuItem>,
    selectedIndex: Int,
    currentTime: String,
    batteryBars: Int,
    signalBars: Int,
    isDetoxActive: Boolean,
    openedItem: NokiaMenuItem?,
    statusBanner: String?,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Quando l'utente preme SU o GIÙ sul tastierino, scorriamo automaticamente la lista LCD
    LaunchedEffect(selectedIndex) {
        if (menuItems.isNotEmpty() && selectedIndex in menuItems.indices) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    // Cornice esterna argentata + vetro LCD incassato
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(NokiaColors.BezelSilver)
            .border(3.dp, NokiaColors.KeyBorderDark, RoundedCornerShape(18.dp))
            .padding(8.dp)
    ) {
        // Schermo a cristalli liquidi vero e proprio
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(NokiaColors.LcdBackground)
                .border(3.dp, NokiaColors.LcdInk, RoundedCornerShape(10.dp))
                // US 1.2: Blocca ogni tocco o scorrimento col dito sul display LCD!
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
        ) {
            // Effetto matrice di pixel (Scanlines orizzontali leggere)
            LcdPixelMatrixOverlay()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Barra verticale sinistra: Tacche del Segnale (4 blocchetti stile Nokia)
                NokiaVerticalBarMeter(
                    activeBars = signalBars,
                    maxBars = 4,
                    bottomSymbol = "Ψ", // Simbolo antenna
                    modifier = Modifier.fillMaxHeight()
                )

                // Contenuto centrale del Display LCD
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Barra superiore: Stato Detox + Indice Menu + Orologio
                    val currentMenuNumber = if (openedItem != null) {
                        "1-${openedItem.indexNumber}"
                    } else {
                        "1-${(selectedIndex + 1).coerceAtMost(menuItems.size)}"
                    }

                    NokiaTopHeader(
                        detoxActive = isDetoxActive,
                        menuIndexText = currentMenuNumber,
                        timeText = currentTime
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Corpo principale del Display: Menu scorrevole oppure Schermata aperta
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        if (openedItem != null) {
                            // Vista dettaglio quando si preme OK su una voce (in attesa di US 1.3 / 1.4)
                            NokiaOpenedItemView(
                                item = openedItem,
                                statusBanner = statusBanner
                            )
                        } else {
                            // Lista Menu principale Nokia
                            LazyColumn(
                                state = listState,
                                userScrollEnabled = false, // Disabilita lo scroll touch
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(menuItems) { index, item ->
                                    NokiaMenuRow(
                                        item = item,
                                        isSelected = (index == selectedIndex)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. Barra inferiore con descrizione voce + etichetta Soft-Key centrale ("Seleziona" / "Indietro")
                    val selectedItem = menuItems.getOrNull(selectedIndex)
                    NokiaBottomSoftKeyBar(
                        subtitle = statusBanner ?: selectedItem?.description.orEmpty(),
                        softKeyLabel = if (openedItem != null) "Indietro" else "Seleziona"
                    )
                }

                // Barra verticale destra: Tacche della Batteria (4 blocchetti stile Nokia)
                NokiaVerticalBarMeter(
                    activeBars = batteryBars,
                    maxBars = 4,
                    bottomSymbol = "▣", // Simbolo batteria
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

/**
 * Disegna una leggera trama di righe a cristalli liquidi sullo sfondo del display.
 */
@Composable
private fun LcdPixelMatrixOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stepPx = 4.dp.toPx()
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = NokiaColors.LcdBackgroundShadow,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
            y += stepPx
        }
    }
}

/**
 * Barra superiore del display LCD con nome modalità, numero indice in alto a destra e orologio.
 */
@Composable
private fun NokiaTopHeader(
    detoxActive: Boolean,
    menuIndexText: String,
    timeText: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (detoxActive) "🔒 NOKIA DETOX" else "MODALITÀ NOKIA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black
            )
            Text(
                text = menuIndexText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black
            )
        }

        // Linea divisoria pixelata + Orologio
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
                    .background(NokiaColors.LcdInk)
            )
            Text(
                text = " $timeText ",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black
            )
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(NokiaColors.LcdInk)
            )
        }
    }
}

/**
 * Singola riga del menu testuale.
 * Quando è selezionata (`isSelected == true`), inverte i colori proprio come sui veri schermi LCD Nokia:
 * sfondo nero-verde (`LcdInk`) e testo verde oliva (`LcdBackground`).
 */
@Composable
private fun NokiaMenuRow(
    item: NokiaMenuItem,
    isSelected: Boolean
) {
    val backgroundColor = if (isSelected) NokiaColors.LcdInk else NokiaColors.LcdBackground
    val contentColor = if (isSelected) NokiaColors.LcdBackground else NokiaColors.LcdInk

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = NokiaColors.LcdInk
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isSelected) "▶" else " ",
            style = MaterialTheme.typography.titleLarge,
            color = contentColor,
            modifier = Modifier.width(24.dp)
        )
        Text(
            text = "${item.indexNumber}. ${item.title}",
            style = MaterialTheme.typography.titleLarge,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Schermata interna mostrata sul display LCD quando si preme OK su una voce del menu.
 */
@Composable
private fun NokiaOpenedItemView(
    item: NokiaMenuItem,
    statusBanner: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(2.dp, NokiaColors.LcdInk)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "[ ${item.title.uppercase()} ]",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = statusBanner ?: item.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Premi il tasto ROSSO (C)\nper tornare al menu",
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Barra inferiore del display LCD con testo di stato e l'etichetta centrale del tasto Soft-Key.
 */
@Composable
private fun NokiaBottomSoftKeyBar(
    subtitle: String,
    softKeyLabel: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(NokiaColors.LcdInk)
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Etichetta centrale tipica del Nokia 3310 sopra il tasto Navi-Key
        Text(
            text = "[ $softKeyLabel ]",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Indicatore verticale a tacche (usato a sinistra per il segnale e a destra per la batteria).
 */
@Composable
private fun NokiaVerticalBarMeter(
    activeBars: Int,
    maxBars: Int,
    bottomSymbol: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Disegna i blocchetti dall'alto (4) verso il basso (1)
            for (barLevel in maxBars downTo 1) {
                val isFilled = barLevel <= activeBars
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(18.dp)
                        .background(if (isFilled) NokiaColors.LcdInk else NokiaColors.LcdBackgroundShadow)
                        .border(1.dp, NokiaColors.LcdInk)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = bottomSymbol,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black
        )
    }
}
