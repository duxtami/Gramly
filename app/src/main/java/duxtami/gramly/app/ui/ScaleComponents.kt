package duxtami.gramly.app.ui

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import duxtami.gramly.app.domain.CalcResult
import duxtami.gramly.app.domain.ResultLine

/** True unless the user has disabled animations system-wide (accessibility). */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) != 0f
    }
}

/**
 * A large, number-only input with a unit prefix (e.g. "₹") and a clear button.
 * Generous height and a rounded shape keep it tappable and on-brand.
 */
@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    prefix: String? = null,
    suffix: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    isError: Boolean = false,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = true,
        isError = isError,
        textStyle = MaterialTheme.typography.headlineSmall,
        prefix = prefix?.let { { Text(it, style = MaterialTheme.typography.titleLarge) } },
        suffix = suffix?.let { { Text(it, style = MaterialTheme.typography.titleMedium) } },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() },
        ),
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Saaf karein")
                }
            }
        },
        shape = MaterialTheme.shapes.large,
    )
}

/**
 * A free-text input styled to match [NumberField] — used for the optional item
 * name on the Rate tab. Words are auto-capitalized; the IME advances to weight.
 */
@Composable
fun NameField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() },
        ),
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Saaf karein")
                }
            }
        },
        shape = MaterialTheme.shapes.large,
    )
}

/**
 * A scrollable row of saved price pills above the price field. Tapping one fills
 * the price; the pencil flips the row into edit mode, where each pill grows a ✕
 * and an inline field appears for typing a new one.
 */
@Composable
fun PricePresetRow(
    presets: List<String>,
    currentPrice: String,
    onSelect: (String) -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    var editing by rememberSaveable { mutableStateOf(false) }
    var draft by rememberSaveable { mutableStateOf("") }

    val commitDraft = {
        onAdd(draft)
        draft = ""
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            presets.forEach { preset ->
                PresetPill(
                    value = preset,
                    selected = preset == currentPrice,
                    editing = editing,
                    onClick = { onSelect(preset) },
                    onRemove = { onRemove(preset) },
                )
            }
            if (editing) {
                PresetDraftField(
                    value = draft,
                    onValueChange = { draft = it.filter(Char::isDigit).take(6) },
                    onCommit = commitDraft,
                )
            } else if (presets.isEmpty()) {
                Text(
                    text = strings.presetsEmpty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        IconButton(
            onClick = {
                if (editing) commitDraft()
                editing = !editing
            },
        ) {
            Icon(
                imageVector = if (editing) Icons.Filled.Done else Icons.Filled.Edit,
                contentDescription = if (editing) strings.presetsDone else strings.presetsEdit,
            )
        }
    }
}

/** One saved price. Shows a ✕ instead of acting as a shortcut while editing. */
@Composable
private fun PresetPill(
    value: String,
    selected: Boolean,
    editing: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val strings = LocalStrings.current
    val source = remember { MutableInteractionSource() }
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clickable(enabled = !editing, interactionSource = source, indication = null) { onClick() }
            .expressivePress(source),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = if (editing) 6.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            if (editing) {
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = strings.presetsRemove,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/** The inline "type a new preset" pill, only present in edit mode. */
@Composable
private fun PresetDraftField(
    value: String,
    onValueChange: (String) -> Unit,
    onCommit: () -> Unit,
) {
    val strings = LocalStrings.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.heightIn(min = 48.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = strings.presetsNew,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        color = LocalContentColor.current,
                        fontWeight = FontWeight.Bold,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onCommit() }),
                    modifier = Modifier.widthIn(min = 56.dp, max = 88.dp),
                )
            }
            IconButton(
                onClick = onCommit,
                enabled = value.isNotEmpty(),
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = strings.presetsAdd,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

/**
 * The hero of each screen: a big card that shows the live answer. It morphs
 * between three states — hint (empty), error, and the result — with an
 * animated value swap so a changing number feels tactile.
 */
@Composable
fun ResultHeroCard(
    label: String,
    result: CalcResult,
    emptyHint: String,
    animationsEnabled: Boolean,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val containerColor = when (result) {
        is CalcResult.Success -> MaterialTheme.colorScheme.primaryContainer
        CalcResult.Error -> MaterialTheme.colorScheme.errorContainer
        CalcResult.Empty -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = when (result) {
        is CalcResult.Success -> MaterialTheme.colorScheme.onPrimaryContainer
        CalcResult.Error -> MaterialTheme.colorScheme.onErrorContainer
        CalcResult.Empty -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        onClick = {
            if (result is CalcResult.Success) onCopy(result.plain)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = contentColor.copy(alpha = 0.7f),
            )

            when (result) {
                is CalcResult.Success ->
                    // Giant weight-morphing hero numeral.
                    HeroNumeral(
                        text = result.display,
                        style = MaterialTheme.typography.displayMedium,
                        color = contentColor,
                    )

                else -> {
                    val target = if (result is CalcResult.Error) strings.errorPriceZero else emptyHint
                    AnimatedContent(
                        targetState = target,
                        transitionSpec = {
                            if (animationsEnabled) {
                                (slideInVertically { it / 2 } + fadeIn())
                                    .togetherWith(slideOutVertically { -it / 2 } + fadeOut())
                            } else {
                                EnterTransition.None togetherWith ExitTransition.None
                            }
                        },
                        label = "result",
                    ) { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            if (result is CalcResult.Success) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = strings.tapToCopy,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}

/** A single labelled output value for [MultiResultCard]. */
data class LabeledValue(val label: String, val line: ResultLine)

/**
 * A hero card for calculations with more than one answer (Piece, Converter).
 * The first value renders as the giant hero numeral; the rest stack below as
 * labelled rows. Tapping the hero or any row copies that value.
 *
 * Pass an empty [values] list for the hint/error states — [isError] picks which.
 */
@Composable
fun MultiResultCard(
    headerLabel: String,
    isError: Boolean,
    emptyHint: String,
    errorText: String,
    values: List<LabeledValue>,
    animationsEnabled: Boolean,
    onCopy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val isSuccess = values.isNotEmpty()
    val containerColor = when {
        isSuccess -> MaterialTheme.colorScheme.primaryContainer
        isError -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = when {
        isSuccess -> MaterialTheme.colorScheme.onPrimaryContainer
        isError -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (isSuccess) {
                val hero = values.first()
                // Hero value — tap anywhere on it to copy.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCopy(hero.line.plain) },
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = hero.label.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor.copy(alpha = 0.7f),
                    )
                    HeroNumeral(
                        text = hero.line.display,
                        style = MaterialTheme.typography.displayMedium,
                        color = contentColor,
                    )
                }

                values.drop(1).forEach { value ->
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = contentColor.copy(alpha = 0.15f),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCopy(value.line.plain) },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = value.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor.copy(alpha = 0.85f),
                        )
                        Text(
                            text = value.line.display,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = strings.tapToCopy,
                        style = MaterialTheme.typography.labelMedium,
                        color = LocalContentColor.current.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            } else {
                Text(
                    text = headerLabel.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor.copy(alpha = 0.7f),
                )
                AnimatedContent(
                    targetState = if (isError) errorText else emptyHint,
                    transitionSpec = {
                        if (animationsEnabled) {
                            (slideInVertically { it / 2 } + fadeIn())
                                .togetherWith(slideOutVertically { -it / 2 } + fadeOut())
                        } else {
                            EnterTransition.None togetherWith ExitTransition.None
                        }
                    },
                    label = "multiResult",
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
