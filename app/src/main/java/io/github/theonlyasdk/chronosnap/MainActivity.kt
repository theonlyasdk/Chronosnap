package io.github.theonlyasdk.chronosnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.background
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.roundToInt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Info
import coil.request.ImageRequest
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.activity.result.IntentSenderRequest
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import com.skydoves.cloudy.cloudy
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import com.skydoves.cloudy.CloudyProgressive
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.github.theonlyasdk.chronosnap.ui.theme.ChronosnapTheme
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val viewModel: PhotoViewModel = viewModel()
            val state by viewModel.state.collectAsState()
            val context = LocalContext.current

            val photoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                viewModel.onImageSelected(uri, context)
            }

            val intentSenderLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    viewModel.saveChanges(context)
                }
            }

            LaunchedEffect(state.pendingIntentSender) {
                state.pendingIntentSender?.let { sender ->
                    intentSenderLauncher.launch(IntentSenderRequest.Builder(sender).build())
                    viewModel.onIntentSenderConsumed()
                }
            }

            LaunchedEffect(state.status) {
                when (val status = state.status) {
                    is PhotoStatus.SaveSuccess -> {
                        snackbarHostState.showSnackbar("Metadata saved successfully!")
                        kotlinx.coroutines.delay(1000.milliseconds)
                        viewModel.onStatusMessageShown()
                    }
                    is PhotoStatus.Error -> {
                        snackbarHostState.showSnackbar(status.message)
                        viewModel.onStatusMessageShown()
                    }
                    else -> {}
                }
            }

            val sky = rememberSky()
            var showSettings by remember { mutableStateOf(false) }
            var dynamicColorsEnabled by rememberSaveable { mutableStateOf(true) }
            val settingsBackProgress = remember { Animatable(0f) }

            val appVersion = rememberAppVersion()
            val showAbout = {
                Toast.makeText(
                    context,
                    "Chronosnap v$appVersion\nDesigned with ❤️ by theonlyasdk",
                    Toast.LENGTH_LONG
                ).show()
            }

            ChronosnapTheme(dynamicColor = dynamicColorsEnabled) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        Box(
                            modifier = Modifier.fillMaxWidth().zIndex(2f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .graphicsLayer {
                                        compositingStrategy = CompositingStrategy.Offscreen
                                    }
                                    .cloudy(sky = sky, radius = 20)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f),
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.20f)
                                            )
                                        )
                                    )
                                    .drawWithContent {
                                        drawContent()
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colorStops = arrayOf(
                                                    0.0f to Color.Black,
                                                    0.7f to Color.Black,
                                                    1.0f to Color.Transparent
                                                )
                                            ),
                                            blendMode = BlendMode.DstIn
                                        )
                                    }
                            )

                            CenterAlignedTopAppBar(
                                title = {
                                    if (showSettings) {
                                        Text("Settings")
                                    } else if (state.selectedUri == null) {
                                        Text("Photo Date Changer")
                                    } else {
                                        Text("Edit Date")
                                    }
                                },
                                navigationIcon = {
                                    if (state.selectedUri != null || showSettings) {
                                        IconButton(
                                            onClick = {
                                                if (showSettings) {
                                                    showSettings = false
                                                } else {
                                                    viewModel.onImageSelected(null, context)
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                                contentDescription = "Back",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    if (state.selectedUri != null && !showSettings) {
                                        val isSuccess = state.status is PhotoStatus.SaveSuccess
                                        val isSaving = state.status is PhotoStatus.Saving

                                        val containerColor by animateColorAsState(
                                            targetValue = if (isSuccess) {
                                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
                                            } else {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                            },
                                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                                            label = "btnBgColor"
                                        )

                                        val contentColor by animateColorAsState(
                                            targetValue = if (isSuccess) {
                                                MaterialTheme.colorScheme.onTertiaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onPrimary
                                            },
                                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                                            label = "btnContentColor"
                                        )

                                        var isPressed by remember { mutableStateOf(false) }
                                        val scale by animateFloatAsState(
                                            targetValue = if (isPressed) 0.92f else 1f,
                                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                                            label = "btnScale"
                                        )

                                        Button(
                                            onClick = { viewModel.saveChanges(context) },
                                            enabled = !isSaving && !isSuccess,
                                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                                containerColor = containerColor,
                                                contentColor = contentColor,
                                                disabledContainerColor = containerColor,
                                                disabledContentColor = contentColor
                                            ),
                                            modifier = Modifier
                                                .padding(0.dp, 0.dp, 5.dp, 0.dp)
                                                .graphicsLayer {
                                                    scaleX = scale
                                                    scaleY = scale
                                                    shape = androidx.compose.foundation.shape.CircleShape
                                                    clip = false
                                                }
                                                .pointerInput(Unit) {
                                                    detectTapGestures(
                                                        onPress = {
                                                            isPressed = true
                                                            tryAwaitRelease()
                                                            isPressed = false
                                                        }
                                                    )
                                                }
                                        ) {
                                            AnimatedContent(
                                                targetState = Triple(isSaving, isSuccess, state.status),
                                                transitionSpec = {
                                                    (fadeIn(tween(200)) + scaleIn(spring(stiffness = Spring.StiffnessMedium))) togetherWith
                                                            (fadeOut(tween(150)) + scaleOut(tween(150)))
                                                },
                                                label = "saveButtonState"
                                            ) { (saving, success, _) ->
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    if (saving) {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier.size(18.dp),
                                                            color = contentColor,
                                                            strokeWidth = 2.dp
                                                        )
                                                    } else {
                                                        Icon(
                                                            imageVector = if (success) Icons.Rounded.Check else Icons.Rounded.Save,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(if (success) "Saved" else "Save")
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (state.selectedUri == null && !showSettings) {
                                        var showMenu by remember { mutableStateOf(false) }
                                        Box {
                                            IconButton(onClick = { showMenu = true }) {
                                                Icon(
                                                    imageVector = Icons.Filled.MoreVert,
                                                    contentDescription = "More options",
                                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showMenu,
                                                onDismissRequest = { showMenu = false }
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Settings") },
                                                    onClick = {
                                                        showMenu = false
                                                        showSettings = true
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("About") },
                                                    onClick = {
                                                        showMenu = false
                                                        showAbout()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    AnimatedContent(
                        targetState = showSettings,
                        modifier = Modifier.graphicsLayer {
                            if (showSettings && settingsBackProgress.value > 0f) {
                                val s = 1f - (settingsBackProgress.value * 0.08f)
                                scaleX = s
                                scaleY = s
                                clip = true
                                shape = RoundedCornerShape((settingsBackProgress.value * 28).dp)
                            }
                        },
                        transitionSpec = {
                            val easeInOutCubic = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
                            val springSpec = spring<IntOffset>(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                            if (targetState) {
                                (slideInHorizontally(animationSpec = springSpec) { width -> width } + fadeIn(tween(150)))
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(350, easing = easeInOutCubic)) { width -> -width / 3 } + fadeOut(tween(350)))
                            } else {
                                (slideInHorizontally(animationSpec = tween(350, easing = easeInOutCubic)) { width -> -width / 3 } + fadeIn(tween(350)))
                                    .togetherWith(slideOutHorizontally(animationSpec = springSpec) { width -> width } + fadeOut(tween(200)))
                            }.apply {
                                targetContentZIndex = if (targetState) 1f else -1f
                            }
                        },
                        label = "SettingsTransitionSpec"
                    ) { settingsActive ->
                        if (settingsActive) {
                            SettingsScreen(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = innerPadding,
                                backProgress = settingsBackProgress,
                                onBack = { showSettings = false },
                                onAbout = showAbout,
                                dynamicColorsEnabled = dynamicColorsEnabled,
                                onDynamicColorsChange = { dynamicColorsEnabled = it }
                            )
                        } else {
                            PhotoDateChangerContent(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = innerPadding,
                                viewModel = viewModel,
                                state = state,
                                sky = sky,
                                onPickImage = {
                                    photoPickerLauncher.launch(arrayOf("image/*"))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDateChangerContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: PhotoViewModel = viewModel(),
    state: PhotoState = viewModel.state.collectAsState().value,
    sky: com.skydoves.cloudy.Sky = rememberSky(),
    onPickImage: () -> Unit = {}
) {
    val context = LocalContext.current
    val backProgress = remember { Animatable(0f) }

    if (state.selectedUri != null) {
        PredictiveBackHandler { progress ->
            try {
                progress.collect { backEvent ->
                    backProgress.snapTo(backEvent.progress)
                }
                viewModel.onImageSelected(null, context)
            } catch (e: CancellationException) {
                backProgress.animateTo(0f)
            } finally {
                backProgress.snapTo(0f)
            }
        }
    }

    AnimatedContent(
        targetState = state.selectedUri,
        modifier = Modifier.graphicsLayer {
            if (state.selectedUri != null && backProgress.value > 0f) {
                val scale = 1f - (backProgress.value * 0.08f)
                scaleX = scale
                scaleY = scale
                clip = true
                shape = RoundedCornerShape((backProgress.value * 28).dp)
            }
        },
        transitionSpec = {
            val easeInOutCubic = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
            val springSpec = spring<IntOffset>(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )

            if (targetState != null) {
                (slideInHorizontally(animationSpec = springSpec) { width -> width } +
                        fadeIn(animationSpec = tween(150)))
                    .togetherWith(
                        slideOutHorizontally(animationSpec = tween(350, easing = easeInOutCubic)) { width -> -width / 3 } +
                                fadeOut(animationSpec = tween(350, easing = easeInOutCubic), targetAlpha = 0.7f)
                    )
            } else {
                (slideInHorizontally(animationSpec = tween(350, easing = easeInOutCubic)) { width -> -width / 3 } +
                        fadeIn(animationSpec = tween(350, easing = easeInOutCubic), initialAlpha = 0.7f))
                    .togetherWith(
                        slideOutHorizontally(animationSpec = springSpec) { width -> width } +
                                fadeOut(animationSpec = tween(200))
                    )
            }.apply {
                targetContentZIndex = if (targetState != null) 1f else -1f
            }
        },
        label = "iOS_ViewTransition"
    ) { currentUri ->
        if (currentUri == null) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(16.dp)
                    .zIndex(0f),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(onPickImage = onPickImage)
            }
        } else {
            val canEdit = state.status is PhotoStatus.Success ||
                    state.status is PhotoStatus.Saving ||
                    state.status is PhotoStatus.SaveSuccess

            Box(modifier = modifier.fillMaxSize().zIndex(1f)) {
                ImagePreviewCard(
                    uri = currentUri,
                    sky = sky,
                    modifier = Modifier.fillMaxSize()
                )

                if (canEdit) {
                    CollapsibleBottomSheet(
                        sky = sky,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        EditMetadataCard(
                            pendingDateTime = state.pendingDateTime,
                            onDateChange = viewModel::onDateChanged,
                            onTimeChange = viewModel::onTimeChanged
                        )
                        Spacer(Modifier.height(16.dp))
                        ExifMetadataContent(metadata = state.metadata)
                    }
                }
            }
        }
    }
}

/**
 * Bottom sheet that starts collapsed to a fixed peek height (drag handle + caller content)
 * and slides up to reveal everything else. Offset is driven directly instead of relying on
 * BottomSheetScaffold, which does not lay out its sheet inside an AnimatedContent.
 */
@Composable
fun CollapsibleBottomSheet(
    sky: com.skydoves.cloudy.Sky,
    modifier: Modifier = Modifier,
    peekHeight: Dp = 152.dp,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val peekPx = with(density) { peekHeight.toPx() }

    var sheetHeightPx by remember { mutableStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }

    // Collapsed = pushed down by the hidden part, expanded = fully on screen.
    val collapsedOffset = (sheetHeightPx - peekPx).coerceAtLeast(0f)

    // While dragging, a short tween tracks the finger almost exactly; on release the
    // spring carries it to the nearest end. One continuous value, so it never jumps.
    val offsetPx by animateFloatAsState(
        targetValue = when {
            dragging -> dragOffsetPx
            expanded -> 0f
            else -> collapsedOffset
        },
        animationSpec = if (dragging) {
            tween(60)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "sheetOffset"
    )

    val dragModifier = Modifier.draggable(
        orientation = Orientation.Vertical,
        state = rememberDraggableState { delta ->
            dragging = true
            dragOffsetPx = (dragOffsetPx + delta).coerceIn(0f, collapsedOffset)
        },
        onDragStopped = { velocity ->
            val target = when {
                velocity < -400f -> 0f
                velocity > 400f -> collapsedOffset
                else -> if (dragOffsetPx < collapsedOffset / 2f) 0f else collapsedOffset
            }
            expanded = target == 0f
            dragging = false
        }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            // Only once expanded does the whole panel become a drag target, so a collapsed
            // sheet does not swallow the vertical drags that pan/zoom the photo behind it.
            .then(if (expanded) dragModifier else Modifier)
            .offset { IntOffset(0, if (sheetHeightPx > 0) offsetPx.roundToInt() else peekPx.roundToInt()) }
            .onSizeChanged { sheetHeightPx = it.height }
            .cloudy(sky = sky, radius = 20)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f))
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
    ) {
        // Drag the handle to expand/collapse; tap the grip to toggle.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        dragging = true
                        dragOffsetPx = (dragOffsetPx + delta).coerceIn(0f, collapsedOffset)
                    },
                    onDragStopped = { velocity ->
                        val target = when {
                            velocity < -400f -> 0f
                            velocity > 400f -> collapsedOffset
                            else -> if (dragOffsetPx < collapsedOffset / 2f) 0f else collapsedOffset
                        }
                        expanded = target == 0f
                        dragging = false
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 24.dp)
                    .clickable { expanded = !expanded },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
fun EmptyState(onPickImage: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "emptyStateScale"
    )

    OutlinedCard(
        onClick = onPickImage,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.AddPhotoAlternate,
                contentDescription = null,
                modifier = Modifier.size(128.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Tap to select a photo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ImagePreviewCard(uri: android.net.Uri, sky: com.skydoves.cloudy.Sky, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier,
        shape = androidx.compose.ui.graphics.RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected photo",
                modifier = Modifier
                    .fillMaxSize()
                    .sky(sky)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val targetScale = (scale.value * zoom).coerceIn(0.5f, 5f)
                            scope.launch {
                                scale.snapTo(targetScale)
                                if (targetScale > 1f) {
                                    offsetX.snapTo(offsetX.value + pan.x)
                                    offsetY.snapTo(offsetY.value + pan.y)
                                } else {
                                    offsetX.snapTo(0f)
                                    offsetY.snapTo(0f)
                                }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            var lastDownTime = 0L
                            var lastDownPosition = Offset.Zero

                            while (true) {
                                val event = awaitPointerEvent()
                                val changes = event.changes

                                if (changes.size > 1) {
                                    lastDownTime = 0L
                                    continue
                                }

                                if (event.type == PointerEventType.Press) {
                                    val change = changes.first()
                                    val currentTime = System.currentTimeMillis()
                                    val position = change.position

                                    if (currentTime - lastDownTime < 300L &&
                                        (position - lastDownPosition).getDistance() < 100f
                                    ) {
                                        var isDrag = false
                                        val currentScale = scale.value
                                        val initialY = position.y

                                        while (true) {
                                            val moveEvent = awaitPointerEvent()
                                            val moveChanges = moveEvent.changes

                                            if (moveChanges.size > 1) {
                                                break
                                            }

                                            val activeChange = moveChanges.find { it.id == change.id }
                                            if (activeChange == null || !activeChange.pressed) {
                                                if (!isDrag) {
                                                    val targetScale = if (scale.value > 1.1f) 1f else 2.5f
                                                    scope.launch {
                                                        scale.animateTo(targetScale)
                                                        offsetX.animateTo(0f)
                                                        offsetY.animateTo(0f)
                                                    }
                                                }
                                                break
                                            }

                                            val dragAmountY = activeChange.position.y - initialY
                                            if (abs(dragAmountY) > 10f) {
                                                isDrag = true
                                            }

                                            if (isDrag) {
                                                val factor = 1f + (dragAmountY * 0.005f)
                                                val targetScale = (currentScale * factor).coerceIn(0.5f, 5f)
                                                scope.launch {
                                                    scale.snapTo(targetScale)
                                                }
                                                activeChange.consume()
                                            }
                                        }
                                        lastDownTime = 0L
                                    } else {
                                        lastDownTime = currentTime
                                        lastDownPosition = position
                                    }
                                }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.changes.all { !it.pressed }) {
                                    if (scale.value < 1f) {
                                        scope.launch {
                                            scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                        }
                                        scope.launch {
                                            offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                        }
                                        scope.launch {
                                            offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale.value,
                        scaleY = scale.value,
                        translationX = offsetX.value,
                        translationY = offsetY.value
                    ),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun ExifMetadataContent(metadata: ExifMetadata?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "EXIF METADATA",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (metadata == null || (metadata.dateOriginal == null && metadata.dateDigitized == null && metadata.dateDateTime == null)) {
            Text(
                text = "No date metadata found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            metadata.dateOriginal?.let {
                Text(
                    text = "Original:  $it",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            metadata.dateDigitized?.let {
                Text(
                    text = "Digitized: $it",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            metadata.dateDateTime?.let {
                Text(
                    text = "Modified:  $it",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMetadataCard(
    pendingDateTime: java.time.LocalDateTime?,
    onDateChange: (Long?) -> Unit,
    onTimeChange: (Int, Int) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
    val timeFormatter = remember(is24Hour) { DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "hh:mm a") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var datePressed by remember { mutableStateOf(false) }
        val dateScale by animateFloatAsState(
            targetValue = if (datePressed) 0.94f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "dateScale"
        )

        Card(
            onClick = { showDatePicker = true },
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    scaleX = dateScale
                    scaleY = dateScale
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            datePressed = true
                            tryAwaitRelease()
                            datePressed = false
                        }
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DATE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pendingDateTime?.format(dateFormatter) ?: "Select Date",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        var timePressed by remember { mutableStateOf(false) }
        val timeScale by animateFloatAsState(
            targetValue = if (timePressed) 0.94f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            label = "timeScale"
        )

        Card(
            onClick = { showTimePicker = true },
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    scaleX = timeScale
                    scaleY = timeScale
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            timePressed = true
                            tryAwaitRelease()
                            timePressed = false
                        }
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TIME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pendingDateTime?.format(timeFormatter) ?: "Select Time",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = pendingDateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateChange(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = pendingDateTime?.hour ?: 0,
            initialMinute = pendingDateTime?.minute ?: 0,
            is24Hour = is24Hour
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChange(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoDateChangerPreview() {
    ChronosnapTheme {
        PhotoDateChangerContent()
    }
}

/** Reads the installed version name from the package manager, e.g. "1.1". */
@Suppress("DEPRECATION")
@Composable
fun rememberAppVersion(): String {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            val pm = context.packageManager
            pm.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "unknown"
    }
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    backProgress: Animatable<Float, *>? = null,
    onBack: () -> Unit = {},
    onAbout: () -> Unit = {},
    dynamicColorsEnabled: Boolean = true,
    onDynamicColorsChange: (Boolean) -> Unit = {}
) {
    if (backProgress != null) {
        PredictiveBackHandler { progress ->
            try {
                progress.collect { backEvent ->
                    backProgress.snapTo(backEvent.progress)
                }
                onBack()
            } catch (e: CancellationException) {
                backProgress.animateTo(0f)
            } finally {
                backProgress.snapTo(0f)
            }
        }
    } else {
        BackHandler {
            onBack()
        }
    }

    var overrideTimeFormat by remember { mutableStateOf(false) }

    val radius = 20.dp
    val topShape = RoundedCornerShape(topStart = radius, topEnd = radius, bottomStart = 4.dp, bottomEnd = 4.dp)
    val bottomShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = radius, bottomEnd = radius)
    val singleShape = RoundedCornerShape(radius)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = topShape
                ) {
                    ListItem(
                        headlineContent = { Text("Use 24-hour time") },
                        supportingContent = { Text("Force 24-hour mode regardless of locale") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = overrideTimeFormat,
                                onCheckedChange = { overrideTimeFormat = it },
                                thumbContent = {
                                    AnimatedContent(targetState = overrideTimeFormat, label = "timeSwitch") { checked ->
                                        if (checked) {
                                            Icon(
                                                imageVector = Icons.Rounded.AccessTime,
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize)
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = bottomShape
                ) {
                    ListItem(
                        headlineContent = { Text("Monet colors") },
                        supportingContent = { Text("Use theme wallpaper colors") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = dynamicColorsEnabled,
                                onCheckedChange = onDynamicColorsChange,
                                thumbContent = {
                                    AnimatedContent(targetState = dynamicColorsEnabled, label = "paletteSwitch") { checked ->
                                        if (checked) {
                                            Icon(
                                                imageVector = Icons.Rounded.Palette,
                                                contentDescription = null,
                                                modifier = Modifier.size(SwitchDefaults.IconSize)
                                            )
                                        }
                                    }
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }

        item {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }

        item {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = singleShape
            ) {
                val appVersion = rememberAppVersion()
                ListItem(
                    headlineContent = { Text("Application info") },
                    modifier = Modifier.clickable(onClick = onAbout),
                    supportingContent = { Text("Version $appVersion") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}
