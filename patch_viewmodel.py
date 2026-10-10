import re

with open("app/src/main/java/duxtami/gramly/app/ui/ScaleViewModel.kt", "r") as f:
    content = f.read()

replacement = """class RateWeightSession(val id: Long = System.nanoTime()) {
    var pricePerKg by mutableStateOf("")
    var amount by mutableStateOf("")
    var rateKilos by mutableStateOf("")
    var rateGrams by mutableStateOf("")
    var itemName by mutableStateOf("")
    var rateItems by mutableStateOf<List<RateItem>>(emptyList())
}

/**
 * Holds calculator state plus the app language. `pricePerKg` is deliberately
"""

content = content.replace("/**\n * Holds calculator state", replacement)

properties_old = """    // Shared across tabs.
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
        private set"""

properties_new = """    // Multi-session support for Rate & Weight
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
        private set(value) { currentSession.itemName = value }"""

content = content.replace(properties_old, properties_new)

rate_items_old = """    // ---- Rate tab running bill -------------------------------------------

    var rateItems by mutableStateOf<List<RateItem>>(emptyList())
        private set"""

rate_items_new = """    // ---- Rate tab running bill -------------------------------------------

    var rateItems: List<RateItem>
        get() = currentSession.rateItems
        private set(value) { currentSession.rateItems = value }"""

content = content.replace(rate_items_old, rate_items_new)

with open("app/src/main/java/duxtami/gramly/app/ui/ScaleViewModel.kt", "w") as f:
    f.write(content)
