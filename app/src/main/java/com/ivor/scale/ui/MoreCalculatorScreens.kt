@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.ivor.scale.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ivor.scale.domain.ConvertResult
import com.ivor.scale.domain.MassUnit
import com.ivor.scale.domain.PieceResult
import kotlinx.coroutines.launch

/**
 * Piece Calculator: "3 bananas = ₹20, how much for 5?" Three inputs feed a
 * two-value result — the total for the wanted count and the per-piece price.
 */
@Composable
fun PieceScreen(
    vm: ScaleViewModel,
    animationsEnabled: Boolean,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val result = vm.pieceResult
    val values = when (result) {
        is PieceResult.Success -> listOf(
            LabeledValue(strings.resultTotalPrice, result.totalPrice),
            LabeledValue(strings.resultPerPiece, result.pricePerPiece),
        )
        else -> emptyList()
    }

    CalculatorPageScaffold(
        title = strings.pieceTitle,
        subtitle = strings.pieceSubtitle,
        onBack = onBack,
    ) { onCopy ->
        NumberField(
            value = vm.pieceKnownQty,
            onValueChange = vm::onPieceKnownQtyChange,
            label = strings.fieldKnownQty,
            isError = result is PieceResult.Error,
        )
        NumberField(
            value = vm.pieceKnownPrice,
            onValueChange = vm::onPieceKnownPriceChange,
            label = strings.fieldKnownPrice,
            prefix = "₹",
        )
        NumberField(
            value = vm.pieceDesiredQty,
            onValueChange = vm::onPieceDesiredQtyChange,
            label = strings.fieldDesiredQty,
            imeAction = ImeAction.Done,
        )

        MultiResultCard(
            headerLabel = strings.resultLabel,
            isError = result is PieceResult.Error,
            emptyHint = strings.hintPiece,
            errorText = strings.errorQtyZero,
            values = values,
            animationsEnabled = animationsEnabled,
            onCopy = onCopy,
        )

        ClearButton(onClear = vm::clearPiece)
    }
}

/**
 * Quantity Price Converter: "2.5 kg = ₹100, what's the real per-kg price?" A
 * quantity (kg or g) and a price expand into per-kg, per-100 g and per-gram.
 */
@Composable
fun ConvertScreen(
    vm: ScaleViewModel,
    animationsEnabled: Boolean,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    val result = vm.convertResult
    val values = when (result) {
        is ConvertResult.Success -> listOf(
            LabeledValue(strings.resultPerKg, result.perKg),
            LabeledValue(strings.resultPer100g, result.per100g),
            LabeledValue(strings.resultPerGram, result.perGram),
        )
        else -> emptyList()
    }
    val unitLabel = if (vm.convertUnit == MassUnit.KG) strings.unitKg else strings.unitGram

    CalculatorPageScaffold(
        title = strings.convertTitle,
        subtitle = strings.convertSubtitle,
        onBack = onBack,
    ) { onCopy ->
        NumberField(
            value = vm.convertQuantity,
            onValueChange = vm::onConvertQuantityChange,
            label = strings.fieldQuantity,
            suffix = unitLabel,
            isError = result is ConvertResult.Error,
        )
        UnitSelector(
            current = vm.convertUnit,
            onSelect = vm::onConvertUnitChange,
        )
        NumberField(
            value = vm.convertPrice,
            onValueChange = vm::onConvertPriceChange,
            label = strings.fieldConvertPrice,
            prefix = "₹",
            imeAction = ImeAction.Done,
        )

        MultiResultCard(
            headerLabel = strings.resultLabel,
            isError = result is ConvertResult.Error,
            emptyHint = strings.hintConvert,
            errorText = strings.errorQtyZero,
            values = values,
            animationsEnabled = animationsEnabled,
            onCopy = onCopy,
        )

        ClearButton(onClear = vm::clearConvert)
    }
}

/**
 * Shared chrome for the single-page calculators: a flexible top app bar with a
 * back button, a copy snackbar, and a scrollable padded column. [content] gets
 * the copy callback so result cards can put values on the clipboard.
 */
@Composable
private fun CalculatorPageScaffold(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable (onCopy: (String) -> Unit) -> Unit,
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboardManager.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val onCopy: (String) -> Unit = { value ->
        clipboard.setText(AnnotatedString(value))
        scope.launch { snackbarHostState.showSnackbar(strings.copiedPrefix + value) }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Black) },
                subtitle = { Text(subtitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            content(onCopy)
        }
    }
}

/** Full-width "Clear" button matching the Rate/Weight tabs' outlined style. */
@Composable
private fun ClearButton(onClear: () -> Unit) {
    val strings = LocalStrings.current
    val source = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClear,
        interactionSource = source,
        modifier = Modifier
            .fillMaxWidth()
            .expressivePress(source),
    ) {
        Text(strings.clear)
    }
}

/** Connected two-button kg / g selector for the converter's quantity unit. */
@Composable
private fun UnitSelector(
    current: MassUnit,
    onSelect: (MassUnit) -> Unit,
) {
    val strings = LocalStrings.current
    val options = listOf(MassUnit.KG to strings.unitKg, MassUnit.G to strings.unitGram)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { index, (unit, label) ->
            ToggleButton(
                checked = current == unit,
                onCheckedChange = { onSelect(unit) },
                shapes = if (index == 0) {
                    ButtonGroupDefaults.connectedLeadingButtonShapes()
                } else {
                    ButtonGroupDefaults.connectedTrailingButtonShapes()
                },
                modifier = Modifier.weight(1f),
            ) {
                Text(label)
            }
        }
    }
}
