package duxtami.gramly.app.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import duxtami.gramly.app.domain.CalcResult
import duxtami.gramly.app.domain.Calculator
import duxtami.gramly.app.domain.ConvertResult
import duxtami.gramly.app.domain.MassUnit
import duxtami.gramly.app.domain.PieceResult

class RateWeightSession(val id: Long = System.nanoTime()) {
    var pricePerKg by mutableStateOf("")
    var amount by mutableStateOf("")
    var rateKilos by mutableStateOf("")
    var rateGrams by mutableStateOf("")
    var itemName by mutableStateOf("")
    var rateItems by mutableStateOf<List<RateItem>>(emptyList())
    var customItemName by mutableStateOf("")
    var customItemAmount by mutableStateOf("")
}

/**
 * Holds calculator state plus the app language. `pricePerKg` is deliberately
 plus the app language. `pricePerKg` is deliberately
 * shared across both tabs — entering the rate once carries over, which is how a
 * real shopkeeper works (one product, two questions).
 */
class ScaleViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("scale_settings", Context.MODE_PRIVATE)

    // Multi-session support for Rate & Weight
    var sessions by mutableStateOf(listOf(RateWeightSession()))
        private set
    var currentSessionIndex by mutableStateOf(0)
        private set
    val currentSession: RateWeightSession
        get() = sessions[currentSessionIndex]

    fun addSession() {
        if (sessions.size < 10) {
            sessions = sessions + RateWeightSession()
            currentSessionIndex = sessions.size - 1
        }
    }

    fun switchSession(index: Int) {
        if (index in sessions.indices) {
            currentSessionIndex = index
        }
    }

    fun removeSession(index: Int) {
        if (sessions.size > 1) {
            sessions = sessions.toMutableList().apply { removeAt(index) }
            if (currentSessionIndex >= sessions.size) {
                currentSessionIndex = sessions.size - 1
            }
        }
    }

    // Shared across tabs.
    var pricePerKg: String
        get() = currentSession.pricePerKg
        private set(value) { currentSession.pricePerKg = value }

    // Weight tab.
    var amount: String
        get() = currentSession.amount
        private set(value) { currentSession.amount = value }

    // Rate tab — weight entered as whole kilos + grams (no unit conversion needed).
    var rateKilos: String
        get() = currentSession.rateKilos
        private set(value) { currentSession.rateKilos = value }
    var rateGrams: String
        get() = currentSession.rateGrams
        private set(value) { currentSession.rateGrams = value }

    // Optional name for the next bill item (e.g. "Tamatar", "Aloo").
    var itemName: String
        get() = currentSession.itemName
        private set(value) { currentSession.itemName = value }

    // Custom/piece item entry for direct amount or items like oranges, bananas, etc.
    var customItemName: String
        get() = currentSession.customItemName
        private set(value) { currentSession.customItemName = value }

    var customItemAmount: String
        get() = currentSession.customItemAmount
        private set(value) { currentSession.customItemAmount = value }

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
    fun onCustomItemNameChange(v: String) { customItemName = v.take(24) }
    fun onCustomItemAmountChange(v: String) { customItemAmount = sanitize(v) }

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
    fun clearRateTab() {
        rateKilos = ""
        rateGrams = ""
        itemName = ""
        customItemName = ""
        customItemAmount = ""
    }
    fun clearPiece() { pieceKnownQty = ""; pieceKnownPrice = ""; pieceDesiredQty = "" }
    fun clearConvert() { convertQuantity = ""; convertPrice = "" }

    // ---- Rate tab running bill -------------------------------------------

    var rateItems: List<RateItem>
        get() = currentSession.rateItems
        private set(value) { currentSession.rateItems = value }

    val rateItemsTotal: Double
        get() = rateItems.sumOf { it.amount }

    /** Save the current weight→amount and/or custom item as bill items, then clear the inputs. */
    fun addRateItem() {
        val price = Calculator.parse(pricePerKg)
        val kilos = Calculator.parse(rateKilos) ?: 0.0
        val grams = Calculator.parse(rateGrams) ?: 0.0
        val weightKg = kilos + grams / 1000.0
        val hasWeight = price != null && weightKg > 0.0

        val customAmt = Calculator.parse(customItemAmount) ?: 0.0
        val hasCustom = customAmt > 0.0

        if (!hasWeight && !hasCustom) return

        val newItems = mutableListOf<RateItem>()
        if (hasWeight) {
            newItems.add(
                RateItem(
                    id = System.nanoTime(),
                    name = itemName.trim(),
                    weightLabel = Calculator.formatWeight(weightKg),
                    amount = price!! * weightKg,
                )
            )
            rateKilos = ""
            rateGrams = ""
            itemName = ""
        }
        if (hasCustom) {
            val name = customItemName.trim()
            newItems.add(
                RateItem(
                    id = System.nanoTime() + 1,
                    name = name,
                    weightLabel = if (name.isBlank()) "Piece / Extra" else "Piece / Fixed",
                    amount = customAmt,
                )
            )
            customItemName = ""
            customItemAmount = ""
        }
        rateItems = rateItems + newItems
    }

    /** Save the custom/piece item (and any pending weight calculation) to the bill. */
    fun addCustomRateItem() {
        addRateItem()
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
