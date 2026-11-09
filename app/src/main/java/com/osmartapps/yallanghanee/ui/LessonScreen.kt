package com.osmartapps.yallanghanee.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.audio.AudioPlayer
import com.osmartapps.yallanghanee.game.RecognitionSession
import com.osmartapps.yallanghanee.ui.components.AnswerCard
import com.osmartapps.yallanghanee.ui.components.AnswerState
import com.osmartapps.yallanghanee.ui.components.ListenOrb
import com.osmartapps.yallanghanee.ui.components.ProgressDots
import com.osmartapps.yallanghanee.ui.theme.NightBg
import com.osmartapps.yallanghanee.ui.theme.NightError
import com.osmartapps.yallanghanee.ui.theme.NightGold
import com.osmartapps.yallanghanee.ui.theme.NightOutlineSoft
import com.osmartapps.yallanghanee.ui.theme.NightSurface
import com.osmartapps.yallanghanee.ui.theme.NightSurfaceHigh
import com.osmartapps.yallanghanee.ui.theme.NightSuccess
import com.osmartapps.yallanghanee.ui.theme.NightText
import com.osmartapps.yallanghanee.ui.theme.NightTextMuted
import com.osmartapps.yallanghanee.ui.theme.maqamColor
import kotlinx.coroutines.delay

private const val SLOW_SPEED = 0.75f

@Composable
fun LessonScreen(
    session: RecognitionSession,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onExit: () -> Unit,
) {
    val context = LocalContext.current
    val player = remember(context) { AudioPlayer(context) }
    var playing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var slow by rememberSaveable { mutableStateOf(false) }
    var comparing by remember { mutableStateOf(false) }
    var audioError by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val question = session.question
    val speed = if (slow) SLOW_SPEED else 1f

    fun stopPlayback() {
        player.stop()
        playing = false
        progress = 0f
        comparing = false
    }

    fun playPhrase() {
        audioError = false
        comparing = false
        progress = 0f
        player.play(
            assetPath = question.audio,
            speed = speed,
            onFinished = {
                playing = false
                progress = 1f
            },
            onInterrupted = {
                playing = false
                progress = 0f
            },
            onError = {
                playing = false
                audioError = true
            },
        )
        playing = player.isPlaying
    }

    fun playComparison() {
        audioError = false
        playing = false
        progress = 0f
        comparing = true
        player.playSequence(
            assetPaths = listOf(question.audio, question.contrastAudio),
            speed = speed,
            onFinished = { comparing = false },
            onInterrupted = { comparing = false },
            onError = {
                comparing = false
                audioError = true
            },
        )
    }

    LaunchedEffect(session.index) {
        scrollState.scrollTo(0)
        if (!session.answered) playPhrase()
    }

    LaunchedEffect(session.answered) {
        if (session.answered) {
            withFrameNanos { }
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(playing, session.index) {
        while (playing && player.isPlaying) {
            val duration = player.durationMs()
            if (duration > 0) {
                progress = (player.positionMs().toFloat() / duration).coerceIn(0f, 1f)
            }
            delay(100)
        }
        if (playing && !player.isPlaying) playing = false
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    BackHandler(enabled = !showExitDialog) { showExitDialog = true }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NightBg, NightSurface)))
            .safeDrawingPadding(),
    ) {
        val compact = maxHeight < 720.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = if (compact) 8.dp else 12.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { showExitDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.lesson_close),
                        tint = NightTextMuted,
                    )
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    ProgressDots(current = session.index, total = session.total)
                }
                SlowToggle(
                    enabled = slow,
                    onToggle = {
                        slow = !slow
                        stopPlayback()
                    },
                )
            }
            Text(
                text = stringResource(
                    R.string.lesson_position,
                    session.index + 1,
                    session.total,
                ),
                color = NightTextMuted,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = stringResource(R.string.lesson_prompt),
                color = NightText,
                style = if (compact) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.headlineMedium
                },
            )
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ListenOrb(
                    isPlaying = playing,
                    progress = progress,
                    enabled = true,
                    diameter = if (compact) 104.dp else 132.dp,
                    onClick = {
                        if (playing) stopPlayback() else playPhrase()
                    },
                )
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(
                        when {
                            comparing -> R.string.lesson_compare_caption
                            playing -> R.string.lesson_playing
                            else -> R.string.lesson_listen
                        },
                        question.answer.nameAr,
                        question.contrast.nameAr,
                    ),
                    color = if (comparing) NightGold else NightTextMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                TextButton(onClick = { playComparison() }) {
                    Text(
                        text = stringResource(R.string.lesson_compare),
                        color = NightGold,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                question.options.forEachIndexed { index, option ->
                    val state = answerState(
                        optionId = option.id,
                        session = session,
                    )
                    AnswerCard(
                        label = option.nameAr,
                        state = state,
                        enabled = !session.answered,
                        onClick = { onAnswer(option.id) },
                        contentDescription = stringResource(
                            R.string.lesson_answer_cd,
                            option.nameAr,
                            index + 1,
                            question.options.size,
                        ),
                    )
                }
            }
            if (session.answered) {
                FeedbackPanel(
                    correct = session.answeredCorrectly,
                    answerName = question.answer.nameAr,
                    explanation = question.explanation,
                    accent = maqamColor(question.answer.id),
                )
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NightGold,
                        contentColor = NightBg,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.lesson_next),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            } else if (audioError) {
                AudioErrorPanel(
                    onRetry = { playPhrase() },
                    onSkip = onSkip,
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        if (showExitDialog) {
            ExitLessonDialog(
                onConfirm = {
                    showExitDialog = false
                    onExit()
                },
                onDismiss = { showExitDialog = false },
            )
        }
    }
}

@Composable
private fun SlowToggle(enabled: Boolean, onToggle: () -> Unit) {
    TextButton(onClick = onToggle) {
        Text(
            text = stringResource(
                if (enabled) R.string.lesson_slow_on else R.string.lesson_slow,
            ),
            color = if (enabled) NightGold else NightTextMuted,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun FeedbackPanel(
    correct: Boolean,
    answerName: String,
    explanation: String,
    accent: androidx.compose.ui.graphics.Color,
) {
    val tone = if (correct) NightSuccess else NightError
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(tone.copy(alpha = 0.12f))
            .border(1.dp, tone.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(
                if (correct) R.string.lesson_correct else R.string.lesson_wrong,
            ),
            color = tone,
            style = MaterialTheme.typography.titleMedium,
        )
        if (!correct) {
            Text(
                text = stringResource(R.string.lesson_answer_is, answerName),
                color = accent,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Text(
            text = explanation,
            color = NightText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun AudioErrorPanel(onRetry: () -> Unit, onSkip: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NightSurfaceHigh)
            .border(1.dp, NightOutlineSoft, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.lesson_audio_error),
            color = NightText,
            style = MaterialTheme.typography.titleSmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.lesson_retry),
                    color = NightGold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            TextButton(onClick = onSkip) {
                Text(
                    text = stringResource(R.string.lesson_skip),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun ExitLessonDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.lesson_exit_title),
                color = NightText,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.lesson_exit_body),
                color = NightTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.lesson_exit_confirm),
                    color = NightGold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.lesson_exit_cancel),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        containerColor = NightSurfaceHigh,
    )
}

private fun answerState(
    optionId: String,
    session: RecognitionSession,
): AnswerState {
    if (!session.answered) return AnswerState.Idle
    val answerId = session.question.answer.id
    return when {
        optionId == session.selectedId && optionId == answerId -> AnswerState.Correct
        optionId == session.selectedId -> AnswerState.Incorrect
        optionId == answerId -> AnswerState.Revealed
        else -> AnswerState.Idle
    }
}
