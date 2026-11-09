package com.osmartapps.yallanghanee.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.audio.MenuMusic
import com.osmartapps.yallanghanee.game.GameState
import com.osmartapps.yallanghanee.ui.theme.BrandCream
import com.osmartapps.yallanghanee.ui.theme.BrandGold
import com.osmartapps.yallanghanee.ui.theme.BrandRedDeep

@Composable
fun HomeScreen(
    game: GameState,
    solvedCount: Int,
    packComplete: Boolean,
    showOnboarding: Boolean,
    onPlay: () -> Unit,
    onResetProgress: () -> Unit,
    onDismissOnboarding: () -> Unit,
) {
    val context = LocalContext.current
    val background = rememberAssetBitmap("img/bg.jpg")
    val logo = rememberAssetBitmap("img/logo.png")
    val guitar by rememberLottieComposition(LottieCompositionSpec.Asset("gfx/guitar.json"))
    val music = remember(context) { runCatching { MenuMusic(context) }.getOrNull() }
    var showResetDialog by remember { mutableStateOf(false) }
    MenuMusicEffect(music)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compact = maxWidth < 360.dp || maxHeight < 640.dp
        if (background != null) {
            Image(
                bitmap = background,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xB32A0000), Color(0xF24B0200)),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 28.dp, vertical = if (compact) 12.dp else 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.5f))
            LottieAnimation(
                composition = guitar,
                clipSpec = LottieClipSpec.Frame(0, 162),
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(if (compact) 110.dp else 150.dp),
            )
            if (logo != null) {
                Image(
                    bitmap = logo,
                    contentDescription = null,
                    modifier = Modifier.size(if (compact) 140.dp else 190.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(
                    text = stringResource(R.string.app_name),
                    color = BrandGold,
                    style = MaterialTheme.typography.displayMedium,
                )
            }
            Text(
                text = stringResource(R.string.home_stage, game.levelIndex + 1, game.totalLevels),
                color = BrandCream,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.home_solved, solvedCount, game.totalLevels),
                color = BrandCream.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelLarge,
            )
            if (packComplete) {
                Text(
                    text = stringResource(R.string.home_complete),
                    color = BrandGold,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth().height(if (compact) 56.dp else 64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandGold,
                    contentColor = BrandRedDeep,
                ),
            ) {
                Text(
                    text = stringResource(R.string.home_play),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            Spacer(Modifier.height(10.dp))
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 320.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.home_footer),
                            color = BrandCream.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall,
                        )
                        TextButton(onClick = { showResetDialog = true }) {
                            Text(
                                text = stringResource(R.string.home_reset),
                                color = BrandCream.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.home_footer),
                                color = BrandCream.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                        TextButton(onClick = { showResetDialog = true }) {
                            Text(
                                text = stringResource(R.string.home_reset),
                                color = BrandCream.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }

        if (showResetDialog) {
            ResetProgressDialog(
                onConfirm = {
                    showResetDialog = false
                    onResetProgress()
                },
                onDismiss = { showResetDialog = false },
            )
        }

        if (showOnboarding) {
            OnboardingDialog(onDismiss = onDismissOnboarding)
        }
    }
}

@Composable
private fun ResetProgressDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.reset_title),
                color = BrandCream,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.reset_message),
                color = BrandCream.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.reset_confirm),
                    color = BrandGold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.reset_cancel),
                    color = BrandCream.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        containerColor = BrandRedDeep,
    )
}

@Composable
private fun OnboardingDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.onboarding_title),
                color = BrandCream,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.onboarding_message),
                color = BrandCream.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.onboarding_start),
                    color = BrandGold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        containerColor = BrandRedDeep,
    )
}

@Composable
private fun MenuMusicEffect(music: MenuMusic?) {
    if (music == null) return
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, music) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> music.start()
                Lifecycle.Event.ON_STOP -> music.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            music.start()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            music.release()
        }
    }
}
