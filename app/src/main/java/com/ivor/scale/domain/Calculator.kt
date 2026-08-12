package com.ivor.scale.domain

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

/**
 * Pure calculation + formatting logic for Scale. No Android dependencies, so it
 * is trivially unit-testable and reused by both tabs.
 *
 * Two inverse operations share one input (price per kg):
 *   Weight tab: weightKg = amount / pricePerKg
 *   Rate  tab: amount   = pricePerKg * weightKg
 */
object Calculator {

    /** Parse a user-typed number; blank / malformed / negative -> null. */
    fun parse(raw: String): Double? {
        val v = raw.trim().replace(",", "").toDoubleOrNull() ?: return null
        return if (v < 0 || v.isNaN() || v.isInfinite()) null else v
    }

    // ---- Weight tab: money -> weight -------------------------------------

    fun weightResult(pricePerKgRaw: String, amountRaw: String): CalcResult {
        val price = parse(pricePerKgRaw)
        val amount = parse(amountRaw)
        if (price == null && amount == null) return CalcResult.Empty
        if (price == null || amount == null) return CalcResult.Empty
        if (price == 0.0) return CalcResult.Error
        val weightKg = amount / price
        return CalcResult.Success(
            display = formatWeight(weightKg),
            plain = formatWeightPlain(weightKg),
        )
    }

    // ---- Rate tab: weight (kg + g) -> money ------------------------------

    fun rateResult(pricePerKgRaw: String, kilosRaw: String, gramsRaw: String): CalcResult {
        val price = parse(pricePerKgRaw) ?: return CalcResult.Empty
        // Need at least one of the weight fields before there's an answer.
        if (kilosRaw.isBlank() && gramsRaw.isBlank()) return CalcResult.Empty
        val kilos = parse(kilosRaw) ?: 0.0
        val grams = parse(gramsRaw) ?: 0.0
        val weightKg = kilos + grams / 1000.0
        if (weightKg <= 0.0) return CalcResult.Empty
        val amount = price * weightKg
        return CalcResult.Success(
            display = formatRupees(amount),
            plain = formatRupeesPlain(amount),
        )
    }

    // ---- Piece tab: known count + price -> price for a desired count ------

    /**
     * "3 bananas = ₹20, how much for 5?" Given a known quantity and its total
     * price, work out the price per piece and the total for the desired count.
     */
    fun pieceResult(
        knownQtyRaw: String,
        knownPriceRaw: String,
        desiredQtyRaw: String,
    ): PieceResult {
        val knownQty = parse(knownQtyRaw)
        val knownPrice = parse(knownPriceRaw)
        val desiredQty = parse(desiredQtyRaw)
        if (knownQty == null || knownPrice == null || desiredQty == null) return PieceResult.Empty
        if (knownQty == 0.0) return PieceResult.Error
        val pricePerPiece = knownPrice / knownQty
        val total = pricePerPiece * desiredQty
        return PieceResult.Success(
            totalPrice = rupeeLine(total),
            pricePerPiece = rupeeLine(pricePerPiece),
        )
    }

    // ---- Converter tab: any quantity + price -> per-kg / 100 g / gram -----

    /**
     * "2.5 kg = ₹100, what's the real per-kg price?" Normalises the quantity to
     * grams, then expresses the price at three comparison-friendly scales.
     */
    fun convertResult(
        quantityRaw: String,
        unit: MassUnit,
        priceRaw: String,
    ): ConvertResult {
        val qty = parse(quantityRaw)
        val price = parse(priceRaw)
        if (qty == null || price == null) return ConvertResult.Empty
        val grams = when (unit) {
            MassUnit.KG -> qty * 1000.0
            MassUnit.G -> qty
        }
        if (grams == 0.0) return ConvertResult.Error
        val perGram = price / grams
        return ConvertResult.Success(
            perKg = rupeeLine(perGram * 1000.0),
            per100g = rupeeLine(perGram * 100.0),
            perGram = rupeeLine(perGram),
        )
    }

    // ---- Formatting ------------------------------------------------------

    private val rupeeFormat: NumberFormat =
        NumberFormat.getNumberInstance(Locale.forLanguageTag("en-IN")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }

    /** 0.5 -> "500 g", 1.25 -> "1 kg 250 g", 2.0 -> "2 kg". */
    fun formatWeight(kg: Double): String {
        val totalGrams = (kg * 1000.0).roundToLong()
        if (totalGrams <= 0L) return "0 g"
        val kgPart = totalGrams / 1000L
        val gPart = totalGrams % 1000L
        return when {
            kgPart == 0L -> "$gPart g"
            gPart == 0L -> "$kgPart kg"
            else -> "$kgPart kg $gPart g"
        }
    }

    private fun formatWeightPlain(kg: Double): String {
        val totalGrams = (kg * 1000.0).roundToLong()
        return "$totalGrams g"
    }

    /** 20.0 -> "₹20", 20.5 -> "₹20.5", 1234.56 -> "₹1,234.56" (Indian grouping). */
    fun formatRupees(amount: Double): String = "₹" + rupeeFormat.format(amount)

    private fun formatRupeesPlain(amount: Double): String {
        val rounded = (amount * 100.0).roundToLong() / 100.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }

    /** A money amount paired for display (hero card) and plain (clipboard) use. */
    private fun rupeeLine(amount: Double): ResultLine =
        ResultLine(display = formatRupees(amount), plain = formatRupeesPlain(amount))
}

/** A unit of mass the Converter accepts as input. */
enum class MassUnit { KG, G }

/** One formatted output value: [display] for the card, [plain] for the clipboard. */
data class ResultLine(val display: String, val plain: String)

/** Outcome of a live calculation. */
sealed interface CalcResult {
    /** Not enough input yet — show a friendly hint, never "NaN". */
    data object Empty : CalcResult

    /** Invalid input (price is 0). The UI supplies the localized message. */
    data object Error : CalcResult

    /** A computed answer. [display] is formatted for the hero card; [plain] for clipboard. */
    data class Success(val display: String, val plain: String) : CalcResult
}

/** Outcome of the Piece calculator. */
sealed interface PieceResult {
    /** Not enough input yet. */
    data object Empty : PieceResult

    /** Invalid input (known quantity is 0). */
    data object Error : PieceResult

    /** Total for the desired count, plus the derived per-piece price. */
    data class Success(val totalPrice: ResultLine, val pricePerPiece: ResultLine) : PieceResult
}

/** Outcome of the Quantity Price converter. */
sealed interface ConvertResult {
    /** Not enough input yet. */
    data object Empty : ConvertResult

    /** Invalid input (quantity is 0). */
    data object Error : ConvertResult

    /** The same price expressed per kilogram, per 100 g, and per gram. */
    data class Success(
        val perKg: ResultLine,
        val per100g: ResultLine,
        val perGram: ResultLine,
    ) : ConvertResult
}
