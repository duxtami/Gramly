package com.ivor.scale.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared scalloped silhouette for the feature-tile icon badges. */
private val CookieBadge = ScallopedShape(lobes = 8, depth = 0.16f)

/**
 * Landing screen. A bold hero header over two editorial feature tiles: each pairs
 * a text column and a scalloped "cookie" icon badge, with the feature's own glyph
 * bleeding oversized behind the content as a graphic anchor and a stadium
 * "open" pill as the affordance.
 */
@Composable
fun HomeScreen(
    onOpenRateWeight: () -> Unit,
    onOpenPiece: () -> Unit,
    onOpenConvert: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val strings = LocalStrings.current
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Hero header ──
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Gramly",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
            )

            // ── Feature tiles ──
            FeatureTile(
                title = strings.homeRateWeightTitle,
                subtitle = strings.homeRateWeightSubtitle,
                icon = Icons.Filled.Calculate,
                container = MaterialTheme.colorScheme.primaryContainer,
                onContainer = MaterialTheme.colorScheme.onPrimaryContainer,
                height = 184.dp,
                onClick = onOpenRateWeight,
            )
            FeatureTile(
                title = strings.homePieceTitle,
                subtitle = strings.homePieceSubtitle,
                icon = Icons.Filled.Numbers,
                container = MaterialTheme.colorScheme.secondaryContainer,
                onContainer = MaterialTheme.colorScheme.onSecondaryContainer,
                height = 184.dp,
                onClick = onOpenPiece,
            )
            FeatureTile(
                title = strings.homeConvertTitle,
                subtitle = strings.homeConvertSubtitle,
                icon = Icons.Filled.Scale,
                container = MaterialTheme.colorScheme.tertiaryContainer,
                onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
                height = 184.dp,
                onClick = onOpenConvert,
            )
            FeatureTile(
                title = strings.homeSettingsTitle,
                subtitle = strings.homeSettingsSubtitle,
                icon = Icons.Filled.Settings,
                container = MaterialTheme.colorScheme.surfaceContainerHigh,
                onContainer = MaterialTheme.colorScheme.onSurface,
                height = 168.dp,
                onClick = onOpenSettings,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    container: Color,
    onContainer: Color,
    height: Dp,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .expressivePress(interactionSource),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = container,
            contentColor = onContainer,
        ),
    ) {
        Box(Modifier.fillMaxSize()) {
            // The feature's own glyph, oversized and tilted, bleeding off the
            // bottom-right corner. The card clips it to its rounded shape.
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = onContainer.copy(alpha = 0.08f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(200.dp)
                    .offset(x = 44.dp, y = 52.dp)
                    .rotate(-14f),
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 22.dp, top = 22.dp, bottom = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = onContainer.copy(alpha = 0.82f),
                        )
                    }
                    OpenPill(onContainer = onContainer)
                }

                // Scalloped icon badge — the expressive focal point.
                Surface(
                    shape = CookieBadge,
                    color = onContainer.copy(alpha = 0.16f),
                    contentColor = onContainer,
                    modifier = Modifier.size(76.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

/** Stadium "Kholein →" affordance — a clear, label-led alternative to a bare arrow button. */
@Composable
private fun OpenPill(onContainer: Color) {
    Surface(
        shape = CircleShape,
        color = onContainer.copy(alpha = 0.14f),
        contentColor = onContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = LocalStrings.current.openLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
