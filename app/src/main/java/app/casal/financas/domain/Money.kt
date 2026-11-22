package app.casal.financas.domain

import java.text.NumberFormat
import java.util.Locale

/** Valores sempre em centavos (Long) para evitar erro de arredondamento. */
object Money {
    private val ptBR = Locale("pt", "BR")

    fun format(cents: Long): String = NumberFormat.getCurrencyInstance(ptBR).format(cents / 100.0)
        .replace(' ', ' ')

    /** "1.234,56" sem símbolo — para campos de edição. */
    fun plain(cents: Long): String = NumberFormat.getNumberInstance(ptBR).apply {
        minimumFractionDigits = 2; maximumFractionDigits = 2
    }.format(cents / 100.0)

    /** Máscara de digitação estilo caixa registradora: "1","2","3" -> 123 centavos. */
    fun centsFromDigits(input: String): Long {
        val digits = input.filter { it.isDigit() }.trimStart('0').take(11)
        return digits.toLongOrNull() ?: 0L
    }
}
