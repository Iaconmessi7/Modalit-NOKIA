package com.retrophonedetox.domain.strategy

import kotlin.random.Random

/**
 * Strategy Pattern (ARCHITECTURE.md - EPIC 4: Sicurezza Anti-Sabotaggio):
 * Interfaccia comune per gli algoritmi anti-impulso eseguibili sul display LCD Nokia
 * tramite i 5 tasti fisici (`▲`, `▼`, `OK`, `≡`, `C / ↩`).
 */
sealed interface UnlockChallenge {
    val title: String
    val progressLabel: String
    val promptLine1: String
    val promptLine2: String
    val options: List<String>
    val correctOptionIndex: Int
}

/**
 * US 4.2 - Enigma Cognitivo di Sblocco per la Pausa di Emergenza (5 min, max 2/giorno).
 * Richiede la risoluzione di `totalSteps` calcoli aritmetici consecutivi.
 */
data class MathPuzzleChallenge(
    val currentStep: Int,
    val totalSteps: Int = 2,
    val expressionText: String,
    override val options: List<String>,
    override val correctOptionIndex: Int
) : UnlockChallenge {
    override val title: String = "ENIGMA SOS (PAUSA 5 MIN)"
    override val progressLabel: String = "Calcolo $currentStep/$totalSteps"
    override val promptLine1: String = "Risolvi per 5m di pausa:"
    override val promptLine2: String = expressionText
}

/**
 * US 4.3 - Il "Custodian System" (Codice Alfanumerico a 20 Caratteri).
 * Verifica i 20 caratteri del Codice Custode suddivisi in 5 blocchi da 4 caratteri
 * (`XXXX-XXXX-XXXX-XXXX-XXXX`) per sbloccare le modifiche alla Whitelist o agli Orari.
 */
data class CustodianCodeChallenge(
    val fullCode20: String,
    val currentBlockIndex: Int,
    val totalBlocks: Int = 5,
    override val options: List<String>,
    override val correctOptionIndex: Int
) : UnlockChallenge {
    private val blocks = fullCode20.split("-")
    private val verifiedChars = currentBlockIndex * 4

    override val title: String = "CODICE CUSTODE (20 CAR.)"
    override val progressLabel: String = "${verifiedChars}/20 car."
    override val promptLine1: String = "${blocks.take(3).joinToString("-")}"
    override val promptLine2: String = "${blocks.drop(3).joinToString("-")} [Blocco ${currentBlockIndex + 1}/5]"
}

/**
 * Interfaccia Strategy per la verifica delle risposte.
 */
interface UnlockChallengeStrategy<T : UnlockChallenge> {
    fun isSelectionCorrect(challenge: T, selectedOptionIndex: Int): Boolean
}

/**
 * US 4.2 - Implementazione `MathPuzzleStrategy`:
 * Genera calcoli aritmetici non banali `(A × B) + C` o `(A × B) - C` con 4 opzioni ravvicinate.
 */
object MathPuzzleStrategy : UnlockChallengeStrategy<MathPuzzleChallenge> {

    fun generateChallenge(step: Int = 1, totalSteps: Int = 2): MathPuzzleChallenge {
        val a = Random.nextInt(11, 24)
        val b = Random.nextInt(3, 9)
        val c = Random.nextInt(12, 45)
        val add = Random.nextBoolean()

        val result = if (add) (a * b) + c else (a * b) - c
        val operatorSymbol = if (add) "+" else "-"
        val expression = "($a × $b) $operatorSymbol $c = ?"

        // Genera 3 distrattori verosimili (es. +-2, +-10, +-4)
        val distractors = mutableSetOf<Int>()
        val offsets = listOf(-10, -4, -2, -1, 1, 2, 4, 10, -6, 6).shuffled()
        for (offset in offsets) {
            if (distractors.size >= 3) break
            val candidate = result + offset
            if (candidate != result && candidate > 0) {
                distractors.add(candidate)
            }
        }

        val allNumbers = (distractors.toList() + result).shuffled()
        val correctIdx = allNumbers.indexOf(result)

        return MathPuzzleChallenge(
            currentStep = step,
            totalSteps = totalSteps,
            expressionText = expression,
            options = allNumbers.mapIndexed { idx, value -> "${idx + 1}. Risultato: $value" },
            correctOptionIndex = correctIdx
        )
    }

    override fun isSelectionCorrect(
        challenge: MathPuzzleChallenge,
        selectedOptionIndex: Int
    ): Boolean = selectedOptionIndex == challenge.correctOptionIndex
}

/**
 * US 4.3 - Implementazione `CustodianCodeStrategy`:
 * Gestisce la generazione di una stringa alfanumerica di 20 caratteri (senza caratteri ambigui O/0/I/1)
 * e la verifica blocco per blocco (5 blocchi × 4 caratteri = 20 caratteri).
 */
object CustodianCodeStrategy : UnlockChallengeStrategy<CustodianCodeChallenge> {

    private const val ALPHANUMERIC_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    /**
     * Genera un codice alfanumerico casuale di 20 caratteri formattato in 5 blocchi da 4:
     * es. `K7M9-X4P2-R8W3-N5T6-Q9B4` (20 caratteri alfanumerici effettivi).
     */
    fun generate20CharCode(): String {
        return (1..5).joinToString("-") {
            (1..4).map {
                ALPHANUMERIC_CHARS[Random.nextInt(ALPHANUMERIC_CHARS.length)]
            }.joinToString("")
        }
    }

    /**
     * Crea la schermata di verifica per il blocco `blockIndex` (da 0 a 4, ciascuno di 4 caratteri).
     * Genera 3 blocchi esca che differiscono per 1-2 caratteri così l'utente deve verificare
     * con attenzione tutti i 20 caratteri del Codice Custode.
     */
    fun createBlockChallenge(fullCode20: String, blockIndex: Int): CustodianCodeChallenge {
        val cleanBlocks = fullCode20.split("-").takeIf { it.size == 5 }
            ?: generate20CharCode().split("-")
        val safeIndex = blockIndex.coerceIn(0, 4)
        val targetBlock = cleanBlocks[safeIndex]

        val decoys = mutableSetOf<String>()
        while (decoys.size < 3) {
            val chars = targetBlock.toCharArray()
            val posToMutate = Random.nextInt(chars.size)
            val replacement = ALPHANUMERIC_CHARS[Random.nextInt(ALPHANUMERIC_CHARS.length)]
            if (replacement != chars[posToMutate]) {
                chars[posToMutate] = replacement
                val candidate = String(chars)
                if (candidate != targetBlock) {
                    decoys.add(candidate)
                }
            }
        }

        val allChoices = (decoys.toList() + targetBlock).shuffled()
        val correctIdx = allChoices.indexOf(targetBlock)

        return CustodianCodeChallenge(
            fullCode20 = cleanBlocks.joinToString("-"),
            currentBlockIndex = safeIndex,
            totalBlocks = 5,
            options = allChoices.mapIndexed { idx, blk ->
                "${idx + 1}. Blocco #${safeIndex + 1}: $blk"
            },
            correctOptionIndex = correctIdx
        )
    }

    override fun isSelectionCorrect(
        challenge: CustodianCodeChallenge,
        selectedOptionIndex: Int
    ): Boolean = selectedOptionIndex == challenge.correctOptionIndex
}
