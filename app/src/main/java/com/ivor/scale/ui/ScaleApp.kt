@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.ivor.scale.ui

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private enum class Screen { Home, RateWeight, Piece, Convert, Settings }

/** Top-level navigator. Lightweight state-based routing across destinations. */
@Composable
fun ScaleApp(vm: ScaleViewModel = viewModel()) {
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    val animationsEnabled = rememberAnimationsEnabled()

    // Predictive back (Android 13+): as the user drags the back gesture we shrink
    // and nudge the current detail screen so dismissal feels physical. Committing
    // the gesture navigates Home; cancelling springs the screen back to rest.
    val backProgress = remember { Animatable(0f) }
    var swipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    PredictiveBackHandler(enabled = screen != Screen.Home) { events ->
        try {
            events.collect { event ->
                swipeEdge = event.swipeEdge
                backProgress.snapTo(event.progress)
            }
            // Gesture committed.
            screen = Screen.Home
            backProgress.snapTo(0f)
        } catch (e: CancellationException) {
            // Gesture cancelled — ease back to rest.
            backProgress.animateTo(0f)
        }
    }

    CompositionLocalProvider(LocalStrings provides stringsFor(vm.language)) {
        // Solid theme-colored backdrop so screen transitions and the predictive
        // back scale-down never reveal the (white) window background.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
        AnimatedContent(
            targetState = screen,
            modifier = Modifier.graphicsLayer {
                val p = if (animationsEnabled) backProgress.value else 0f
                if (p > 0f) {
                    val scale = lerp(1f, 0.9f, p)
                    scaleX = scale
                    scaleY = scale
                    translationX =
                        (if (swipeEdge == BackEventCompat.EDGE_LEFT) 1f else -1f) * p * 32.dp.toPx()
                    val corner = lerp(0f, 28.dp.toPx(), p)
                    shape = RoundedCornerShape(corner)
                    clip = true
                    alpha = lerp(1f, 0.6f, p)
                }
            },
            transitionSpec = {
                if (!animationsEnabled) {
                    EnterTransition.None togetherWith ExitTransition.None
                } else {
                    val forward = targetState.ordinal > initialState.ordinal
                    val dir = if (forward) 1 else -1
                    (slideInHorizontally { w -> dir * w } + fadeIn())
                        .togetherWith(slideOutHorizontally { w -> -dir * w } + fadeOut())
                }
            },
            label = "screen",
        ) { current ->
            when (current) {
                Screen.Home -> HomeScreen(
                    onOpenRateWeight = { screen = Screen.RateWeight },
                    onOpenPiece = { screen = Screen.Piece },
                    onOpenConvert = { screen = Screen.Convert },
                    onOpenSettings = { screen = Screen.Settings },
                )

                Screen.RateWeight -> RateWeightScreen(
                    vm = vm,
                    animationsEnabled = animationsEnabled,
                    onBack = { screen = Screen.Home },
                )

                Screen.Piece -> PieceScreen(
                    vm = vm,
                    animationsEnabled = animationsEnabled,
                    onBack = { screen = Screen.Home },
                )

                Screen.Convert -> ConvertScreen(
                    vm = vm,
                    animationsEnabled = animationsEnabled,
                    onBack = { screen = Screen.Home },
                )

                Screen.Settings -> SettingsScreen(
                    currentLanguage = vm.language,
                    onLanguageChange = vm::changeLanguage,
                    onBack = { screen = Screen.Home },
                )
            }
        }
        }
    }
}

@Composable
private fun RateWeightScreen(
    vm: ScaleViewModel,
    animationsEnabled: Boolean,
    onBack: () -> Unit,
) {
    val strings = LocalStrings.current
    // Rate first, then Weight.
    val tabLabels = listOf(strings.tabRate, strings.tabWeight)
    val pagerState = rememberPagerState(pageCount = { tabLabels.size })
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
                title = { Text(strings.rateWeightTitle, fontWeight = FontWeight.Black) },
                subtitle = { Text(strings.rateWeightSubtitle) },
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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Connected button group as the Rate / Weight switcher.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                tabLabels.forEachIndexed { index, title ->
                    ToggleButton(
                        checked = pagerState.currentPage == index,
                        onCheckedChange = { scope.launch { pagerState.animateScrollToPage(index) } },
                        shapes = if (index == 0) {
                            ButtonGroupDefaults.connectedLeadingButtonShapes()
                        } else {
                            ButtonGroupDefaults.connectedTrailingButtonShapes()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(title)
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                // On the last input field, the IME action jumps to the other tab.
                val onImeNext: () -> Unit = {
                    scope.launch { pagerState.animateScrollToPage(1 - page) }
                }
                when (page) {
                    0 -> RateTab(vm, animationsEnabled, onCopy, onImeNext)
                    else -> WeightTab(vm, animationsEnabled, onCopy, onImeNext)
                }
            }
        }
    }
}
