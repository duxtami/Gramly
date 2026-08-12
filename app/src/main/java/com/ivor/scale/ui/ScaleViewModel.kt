package com.ivor.scale.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.ivor.scale.domain.CalcResult
import com.ivor.scale.domain.Calculator
import com.ivor.scale.domain.ConvertResult
import com.ivor.scale.domain.MassUnit
import com.ivor.scale.domain.PieceResult

/**
 * Holds calculator state plus the app language. `pricePerKg` is deliberately
 * shared across both tabs — entering the rate once carries over, which is how a
 * real shopkeeper works (one product, two questions).
 */
class ScaleViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("scale_settings", Context.MODE_PRIVATE)

    // Shared across tabs.
    var pricePerKg by mutableStateOf("")
        private set

    // Weight tab.
    var amount by mutableStateOf("")
        private set

    // Rate tab — weight entered as whole kilos + grams (no unit conversion needed).
    var rateKilos by mutableStateOf("")
        private set
    var rateGrams by mutableStateOf("")
        private set

    // Optional name for the next bill item (e.g. "Tamatar", "Aloo").
    var itemName by mutableStateOf("")
        private set

    // Piece calculator — known count + its price, and the count the user wants.
    var pieceKnownQty by mutableStateOf("")
        private set
    var pieceKnownPrice by mutableStateOf("")
        private set
    var pieceDesiredQty by mutableStateOf("")
        private set

    // Quantity Price converter — a quantity (in kg or g) and its price.
    var convertQuantity by mutableStateOf("")
        private set
    var convertPrice by mutableStateOf("")
        private set
    var convertUnit by mutableStateOf(MassUnit.KG)
        private set

    // App language (persisted across launches).
    var language by mutableStateOf(loadLanguage())
        private set

    // Quick price presets, shown as pills above the price field (persisted).
    var pricePresets by mutableStateOf(loadPresets())
        private set

    fun onPriceChange(v: String) { pricePerKg = sanitize(v) }
    fun onAmountChange(v: String) { amount = sanitize(v) }
    fun onRateKilosChange(v: String) { rateKilos = sanitize(v) }
    fun onRateGramsChange(v: String) { rateGrams = sanitize(v) }
    fun onItemNameChange(v: String) { itemName = v.take(24) }

    fun onPieceKnownQtyChange(v: String) { pieceKnownQty = sanitize(v) }
    fun onPieceKnownPriceChange(v: String) { pieceKnownPrice = sanitize(v) }
    fun onPieceDesiredQtyChange(v: String) { pieceDesiredQty = sanitize(v) }

    fun onConvertQuantityChange(v: String) { convertQuantity = sanitize(v) }
    fun onConvertPriceChange(v: String) { convertPrice = sanitize(v) }
    fun onConvertUnitChange(unit: MassUnit) { convertUnit = unit }

    val weightResult: CalcResult
        get() = Calculator.weightResult(pricePerKg, amount)

    val rateResult: CalcResult
        get() = Calculator.rateResult(pricePerKg, rateKilos, rateGrams)

    val pieceResult: PieceResult
        get() = Calculator.pieceResult(pieceKnownQty, pieceKnownPrice, pieceDesiredQty)

    val convertResult: ConvertResult
        get() = Calculator.convertResult(convertQuantity, convertUnit, convertPrice)

    fun clearWeightTab() { amount = "" }
    fun clearRateTab() { rateKilos = ""; rateGrams = ""; itemName = "" }
    fun clearPiece() { pieceKnownQty = ""; pieceKnownPrice = ""; pieceDesiredQty = "" }
    fun clearConvert() { convertQuantity = ""; convertPrice = "" }

    // ---- Rate tab running bill -------------------------------------------

    var rateItems by mutableStateOf<List<RateItem>>(emptyList())
        private set

    val rateItemsTotal: Double
        get() = rateItems.sumOf { it.amount }

    /** Save the current weight→amount as a bill item, then clear the weight inputs. */
    fun addRateItem() {
        val price = Calculator.parse(pricePerKg) ?: return
        val kilos = Calculator.parse(rateKilos) ?: 0.0
        val grams = Calculator.parse(rateGrams) ?: 0.0
        val weightKg = kilos + grams / 1000.0
        if (weightKg <= 0.0) return
        val item = RateItem(
            id = System.nanoTime(),
            name = itemName.trim(),
            weightLabel = Calculator.formatWeight(weightKg),
            amount = price * weightKg,
        )
        rateItems = rateItems + item
        rateKilos = ""
        rateGrams = ""
        itemName = ""
    }

    fun removeRateItem(id: Long) {
        rateItems = rateItems.filterNot { it.id == id }
    }

    fun clearRateItems() {
        rateItems = emptyList()
    }

    // ---- Price presets ----------------------------------------------------

    /** Tap a pill: it just fills the shared price field. */
    fun applyPricePreset(value: String) { pricePerKg = sanitize(value) }

    /** Add a pill. Ignores blanks, zeros and duplicates; caps the list length. */
    fun addPricePreset(value: String) {
        val clean = sanitize(value).trimEnd('.')
        if (clean.isEmpty() || Calculator.parse(clean).let { it == null || it <= 0.0 }) return
        if (clean in pricePresets || pricePresets.size >= MAX_PRESETS) return
        pricePresets = pricePresets + clean
        savePresets()
    }

    fun removePricePreset(value: String) {
        pricePresets = pricePresets - value
        savePresets()
    }

    private fun savePresets() {
        prefs.edit().putString(KEY_PRESETS, pricePresets.joinToString(",")).apply()
    }

    private fun loadPresets(): List<String> {
        val stored = prefs.getString(KEY_PRESETS, null) ?: return DEFAULT_PRESETS
        // An empty string is a valid state — the user removed every preset.
        return stored.split(",").filter { it.isNotBlank() }
    }

    fun changeLanguage(lang: Language) {
        language = lang
        prefs.edit().putString(KEY_LANGUAGE, lang.name).apply()
    }

    private fun loadLanguage(): Language = runCatching {
        Language.valueOf(prefs.getString(KEY_LANGUAGE, Language.HINGLISH.name)!!)
    }.getOrDefault(Language.HINGLISH)

    /** Keep only digits and a single decimal point; trim absurd lengths. */
    private fun sanitize(input: String): String {
        val filtered = buildString {
            var dotSeen = false
            for (ch in input) {
                when {
                    ch.isDigit() -> append(ch)
                    (ch == '.' || ch == ',') && !dotSeen -> { append('.'); dotSeen = true }
                }
            }
        }
        return filtered.take(12)
    }

    companion object {
        private const val KEY_LANGUAGE = "language"
        private const val KEY_PRESETS = "price_presets"
        private const val MAX_PRESETS = 8
        private val DEFAULT_PRESETS = listOf("50", "100")
    }
}

/** One saved line in the Rate-tab running bill. */
data class RateItem(
    val id: Long,
    val name: String,
    val weightLabel: String,
    val amount: Double,
)
