package com.osmartapps.yallanghanee.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.audio.AudioPlayer
import com.osmartapps.yallanghanee.audio.SoundEffects
import com.osmartapps.yallanghanee.game.AnswerSlot
import com.osmartapps.yallanghanee.game.GameState
import com.osmartapps.yallanghanee.game.TRAY_ROW_SIZE
import com.osmartapps.yallanghanee.ui.theme.BrandCream
import com.osmartapps.yallanghanee.ui.theme.BrandError
import com.osmartapps.yallanghanee.ui.theme.BrandGold
import com.osmartapps.yallanghanee.ui.theme.BrandRedDeep

@Composable
fun GameScreen(
    game: GameState,
    question: String,
    packComplete: Boolean,
    onBack: () -> Unit,
    onTrayLetter: (Int) -> Unit,
    onSlotClick: (Int, Int) -> Unit,
    onNext: () -> Unit,
    onResetProgress: () -> Unit,
    onHint: () -> Unit,
) {
    val context = LocalContext.current
    val background = rememberAssetBitmap("img/bg.jpg")
    val player = remember { AudioPlayer(context) }
    val soundEffects = remember { runCatching { SoundEffects(context) }.getOrNull() }
    var isPlaying by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    DisposableEffect(Unit) {
        onDispose {
            player.release()
            soundEffects?.release()
        }
    }

    LaunchedEffect(game.levelId) {
        player.stop()
        isPlaying = false
    }

    LaunchedEffect(game.solved) {
        if (game.solved) {
            player.stop()
            isPlaying = false
            soundEffects?.playWin()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                player.stop()
                isPlaying = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val togglePlayback: () -> Unit = {
        isPlaying = player.toggle(
            assetPath = "content/audio/${game.audioFile}",
            onFinished = { isPlaying = false },
            onInterrupted = { isPlaying = false },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (background != null) {
            ImageBackground(background)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xCC1E0000), Color(0xF24B0200)),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            GameTopBar(game = game, onBack = onBack)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SongControls(
                    isPlaying = isPlaying,
                    onToggle = togglePlayback,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = question,
                    color = BrandGold,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(18.dp))
                AnswerBoard(
                    game = game,
                    onSlotClick = onSlotClick,
                )
                Spacer(Modifier.height(14.dp))
                HintButton(
                    enabled = !game.solved && !game.wrong && game.filledSlots < game.totalSlots,
                    onClick = {
                        if (isPlaying) {
                            player.stop()
                            isPlaying = false
                        }
                        onHint()
                    },
                )
                AnimatedVisibility(visible = game.wrong) {
                    Text(
                        text = stringResource(R.string.game_wrong),
                        color = BrandError,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 14.dp),
                    )
                }
            }

            TrayBoard(
                game = game,
                onTrayLetter = onTrayLetter,
            )
        }

        if (game.solved) {
            WinOverlay(
                game = game,
                packComplete = packComplete,
                onNext = onNext,
                onResetProgress = onResetProgress,
            )
        }
    }
}

@Composable
private fun ImageBackground(bitmap: androidx.compose.ui.graphics.ImageBitmap) {
    androidx.compose.foundation.Image(
        bitmap = bitmap,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun GameTopBar(game: GameState, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.game_back),
                tint = BrandCream,
            )
        }
        Spacer(Modifier.weight(1f))
        Surface(
            color = Color(0x66000000),
            shape = RoundedCornerShape(50),
        ) {
            Text(
                text = stringResource(R.string.game_stage, game.levelIndex + 1, game.totalLevels),
                color = BrandGold,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun SongControls(isPlaying: Boolean, onToggle: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "songPulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "songPulseScale",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(pulse)
                        .background(BrandGold.copy(alpha = 0.16f), CircleShape),
                )
            }
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(BrandGold)
                    .clickable(onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = stringResource(
                        if (isPlaying) R.string.game_pause else R.string.play_song_cd,
                    ),
                    tint = BrandRedDeep,
                    modifier = Modifier.size(38.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(if (isPlaying) R.string.game_pause else R.string.game_listen),
            color = BrandCream.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun HintButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandGold.copy(alpha = 0.18f),
            contentColor = BrandGold,
            disabledContainerColor = BrandCream.copy(alpha = 0.08f),
            disabledContentColor = BrandCream.copy(alpha = 0.35f),
        ),
    ) {
        Icon(
            imageVector = Icons.Filled.Lightbulb,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.game_hint),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun AnswerBoard(game: GameState, onSlotClick: (Int, Int) -> Unit) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        val spacing = 5.dp
        val maxLetters = game.words.maxOfOrNull { it.slots.size } ?: 1
        val slotSize = ((maxWidth - spacing * (maxLetters - 1)) / maxLetters).coerceIn(18.dp, 48.dp)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            game.words.forEachIndexed { wordIndex, word ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    word.slots.forEachIndexed { slotIndex, slot ->
                        SlotBox(
                            slot = slot,
                            wrong = game.wrong,
                            size = slotSize,
                            onClick = { onSlotClick(wordIndex, slotIndex) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlotBox(
    slot: AnswerSlot,
    wrong: Boolean,
    size: Dp,
    onClick: () -> Unit,
) {
    val borderColor = when {
        wrong -> BrandError
        slot.filled != null -> BrandGold
        else -> BrandCream.copy(alpha = 0.55f)
    }
    val background = if (slot.filled != null) Color(0xCC2A0000) else Color(0x66000000)
    val scale by animateFloatAsState(
        targetValue = if (wrong) 1.06f else 1f,
        label = "slotScale",
    )
    val description = if (slot.filled != null) {
        stringResource(R.string.slot_filled_cd, slot.filled.toString())
    } else {
        stringResource(R.string.slot_empty_cd)
    }

    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .clip(RoundedCornerShape(9.dp))
            .background(background)
            .border(2.dp, borderColor, RoundedCornerShape(9.dp))
            .clickable(enabled = slot.filled != null, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = slot.filled?.toString().orEmpty(),
            color = BrandCream,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun TrayBoard(game: GameState, onTrayLetter: (Int) -> Unit) {
    val disabled = game.solved || game.wrong
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        val spacing = 5.dp
        val tileSize = ((maxWidth - spacing * (TRAY_ROW_SIZE - 1)) / TRAY_ROW_SIZE)
            .coerceIn(32.dp, 48.dp)
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            game.tray.withIndex().chunked(TRAY_ROW_SIZE).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    row.forEach { (index, letter) ->
                        TrayTile(
                            letter = letter.letter,
                            disabled = letter.used || disabled,
                            size = tileSize,
                            onClick = { onTrayLetter(index) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrayTile(
    letter: Char,
    disabled: Boolean,
    size: Dp,
    onClick: () -> Unit,
) {
    val description = stringResource(R.string.letter_cd, letter.toString())
    Box(
        modifier = Modifier
            .size(size)
            .alpha(if (disabled) 0.28f else 1f)
            .clip(RoundedCornerShape(8.dp))
            .background(BrandCream.copy(alpha = 0.12f))
            .border(1.dp, BrandGold.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
            .clickable(enabled = !disabled, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = letter.toString(),
            color = BrandCream,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun WinOverlay(
    game: GameState,
    packComplete: Boolean,
    onNext: () -> Unit,
    onResetProgress: () -> Unit,
) {
    val animation by rememberLottieComposition(LottieCompositionSpec.Asset("gfx/winanim.json"))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6000000))
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LottieAnimation(
                composition = animation,
                iterations = 1,
                modifier = Modifier.size(200.dp),
            )
            Text(
                text = stringResource(
                    if (packComplete) R.string.game_completed else R.string.game_correct,
                ),
                color = BrandGold,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = game.title,
                color = BrandCream,
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(26.dp))
            Button(
                onClick = if (packComplete) onResetProgress else onNext,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGold,
                    contentColor = BrandRedDeep,
                ),
            ) {
                Text(
                    text = stringResource(
                        if (packComplete) R.string.game_start_over else R.string.game_next,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                )
            }
        }
    }
}
