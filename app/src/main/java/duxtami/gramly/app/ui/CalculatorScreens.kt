package duxtami.gramly.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import duxtami.gramly.app.domain.CalcResult
import duxtami.gramly.app.domain.Calculator

/** Weight tab: money in, weight out. */
@Composable
fun WeightTab(
    vm: ScaleViewModel,
    animationsEnabled: Boolean,
    onCopy: (String) -> Unit,
    onImeNext: () -> Unit,
) {
    val strings = LocalStrings.current
    val result = vm.weightResult
    CalculatorScaffold(
        result = result,
        resultLabel = strings.resultWeight,
        emptyHint = strings.hintWeight,
        onClear = { vm.clearWeightTab() },
        animationsEnabled = animationsEnabled,
        onCopy = onCopy,
    ) {
        PricePresetRow(
            presets = vm.pricePresets,
            currentPrice = vm.pricePerKg,
            onSelect = vm::applyPricePreset,
            onAdd = vm::addPricePreset,
            onRemove = vm::removePricePreset,
        )
        NumberField(
            value = vm.pricePerKg,
            onValueChange = vm::onPriceChange,
            label = strings.fieldPrice,
            prefix = "₹",
            isError = result is CalcResult.Error,
        )
        NumberField(
            value = vm.amount,
            onValueChange = vm::onAmountChange,
            label = strings.fieldAmount,
            prefix = "₹",
            imeAction = ImeAction.Next,
            onImeAction = onImeNext,
        )
    }
}

/** Rate tab: weight in, money out — plus a running bill. */
@Composable
fun RateTab(
    vm: ScaleViewModel,
    animationsEnabled: Boolean,
    onCopy: (String) -> Unit,
    onImeNext: () -> Unit,
) {
    val strings = LocalStrings.current
    val result = vm.rateResult
    CalculatorScaffold(
        result = result,
        resultLabel = strings.resultAmount,
        emptyHint = strings.hintAmount,
        onClear = { vm.clearRateTab() },
        animationsEnabled = animationsEnabled,
        onCopy = onCopy,
        onAdd = { vm.addRateItem() },
        addEnabled = (result is CalcResult.Success) || vm.customItemAmount.isNotBlank(),
        belowButtons = {
            CustomItemSection(
                name = vm.customItemName,
                amount = vm.customItemAmount,
                onNameChange = vm::onCustomItemNameChange,
                onAmountChange = vm::onCustomItemAmountChange,
                onAdd = { vm.addCustomRateItem() },
                canAdd = vm.customItemAmount.isNotBlank() || (result is CalcResult.Success),
            )

            if (vm.rateItems.isNotEmpty()) {
                RateBillSection(
                    items = vm.rateItems,
                    total = vm.rateItemsTotal,
                    onRemove = vm::removeRateItem,
                    onClearAll = vm::clearRateItems,
                    onCopy = onCopy,
                )
            }
        },
    ) {
        PricePresetRow(
            presets = vm.pricePresets,
            currentPrice = vm.pricePerKg,
            onSelect = vm::applyPricePreset,
            onAdd = vm::addPricePreset,
            onRemove = vm::removePricePreset,
        )
        NumberField(
            value = vm.pricePerKg,
            onValueChange = vm::onPriceChange,
            label = strings.fieldPrice,
            prefix = "₹",
        )
        NameField(
            value = vm.itemName,
            onValueChange = vm::onItemNameChange,
            label = strings.fieldItemName,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(
                value = vm.rateKilos,
                onValueChange = vm::onRateKilosChange,
                label = strings.fieldKilo,
                suffix = "kg",
                modifier = Modifier.weight(1f),
            )
            NumberField(
                value = vm.rateGrams,
                onValueChange = vm::onRateGramsChange,
                label = strings.fieldGram,
                suffix = "g",
                imeAction = ImeAction.Next,
                onImeAction = onImeNext,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Shared layout for both tabs: scrollable column with input fields, the result
 * hero card, the action buttons (Clear, and optionally Add), and any extra
 * content below them (the Rate bill).
 */
@Composable
private fun CalculatorScaffold(
    result: CalcResult,
    resultLabel: String,
    emptyHint: String,
    onClear: () -> Unit,
    animationsEnabled: Boolean,
    onCopy: (String) -> Unit,
    onAdd: (() -> Unit)? = null,
    addEnabled: Boolean = false,
    belowButtons: (@Composable () -> Unit)? = null,
    inputs: @Composable () -> Unit,
) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        inputs()

        ResultHeroCard(
            label = resultLabel,
            result = result,
            emptyHint = emptyHint,
            animationsEnabled = animationsEnabled,
            onCopy = onCopy,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val clearSource = remember { MutableInteractionSource() }
            OutlinedButton(
                onClick = onClear,
                interactionSource = clearSource,
                modifier = Modifier
                    .weight(1f)
                    .expressivePress(clearSource),
            ) {
                Text(strings.clear)
            }
            if (onAdd != null) {
                val addSource = remember { MutableInteractionSource() }
                Button(
                    onClick = onAdd,
                    enabled = addEnabled,
                    interactionSource = addSource,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    modifier = Modifier
                        .weight(1f)
                        .expressivePress(addSource),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        strings.add,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        belowButtons?.invoke()

        Text(
            text = strings.tipSharedPrice,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
    }
}

/**
 * Card for entering a custom amount (e.g. for bananas, oranges, fixed items) directly into the bill.
 */
@Composable
private fun CustomItemSection(
    name: String,
    amount: String,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onAdd: () -> Unit,
    canAdd: Boolean,
) {
    val strings = LocalStrings.current
    val addSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = strings.customItemSectionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                NameField(
                    value = name,
                    onValueChange = onNameChange,
                    label = strings.fieldCustomName,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1.1f),
                )
                NumberField(
                    value = amount,
                    onValueChange = onAmountChange,
                    label = strings.fieldCustomAmount,
                    prefix = "₹",
                    imeAction = ImeAction.Done,
                    onImeAction = onAdd,
                    modifier = Modifier.weight(1f),
                )
            }

            Button(
                onClick = onAdd,
                enabled = canAdd,
                interactionSource = addSource,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                modifier = Modifier
                    .fillMaxWidth()
                    .expressivePress(addSource),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.addCustomItem)
            }
        }
    }
}

/** The running bill: each saved weight→amount line, a grand total, and clear-all. */
@Composable
private fun RateBillSection(
    items: List<RateItem>,
    total: Double,
    onRemove: (Long) -> Unit,
    onClearAll: () -> Unit,
    onCopy: (String) -> Unit,
) {
    val strings = LocalStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${strings.billHeader} (${items.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(onClick = onClearAll) {
                    Text(strings.billClearAll)
                }
            }

            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (item.name.isNotBlank()) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (item.weightLabel.isNotBlank()) {
                                Text(
                                    text = item.weightLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            Text(
                                text = item.weightLabel.ifBlank { strings.customItemDefaultLabel },
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                    Text(
                        text = Calculator.formatRupees(item.amount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    IconButton(
                        onClick = { onRemove(item.id) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = strings.billClearAll,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCopy(Calculator.formatRupeesPlain(total)) },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = strings.billTotal,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = strings.tapToCopy,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        )
                    }
                    Text(
                        text = Calculator.formatRupees(total),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}
