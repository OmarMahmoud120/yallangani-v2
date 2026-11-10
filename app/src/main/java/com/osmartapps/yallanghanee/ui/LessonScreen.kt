package com.osmartapps.yallanghanee.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.audio.AudioPlayer
import com.osmartapps.yallanghanee.audio.hasNetwork
import com.osmartapps.yallanghanee.audio.isRemoteSource
import com.osmartapps.yallanghanee.game.RecognitionEngine
import com.osmartapps.yallanghanee.game.RecognitionSession
import com.osmartapps.yallanghanee.model.name
import com.osmartapps.yallanghanee.ui.components.AnswerCard
import com.osmartapps.yallanghanee.ui.components.AudioErrorPanel
import com.osmartapps.yallanghanee.ui.components.DroneToggle
import com.osmartapps.yallanghanee.ui.components.ExitLessonDialog
import com.osmartapps.yallanghanee.ui.components.FeedbackPanel
import com.osmartapps.yallanghanee.ui.components.ListenOrb
import com.osmartapps.yallanghanee.ui.components.PrimaryActionButton
import com.osmartapps.yallanghanee.ui.components.ProgressDots
import com.osmartapps.yallanghanee.ui.components.SlowToggle
import com.osmartapps.yallanghanee.ui.components.answerState
import com.osmartapps.yallanghanee.ui.theme.NightBg
import com.osmartapps.yallanghanee.ui.theme.NightGold
import com.osmartapps.yallanghanee.ui.theme.NightSurface
import com.osmartapps.yallanghanee.ui.theme.NightText
import com.osmartapps.yallanghanee.ui.theme.NightTextMuted
import com.osmartapps.yallanghanee.ui.theme.maqamColor
import kotlinx.coroutines.delay

private const val SLOW_SPEED = 0.75f
private const val DRONE_VOLUME = 0.32f

@Composable
fun LessonScreen(
    session: RecognitionSession,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onExit: () -> Unit,
    onHint: () -> Unit = {},
    hintUnavailable: Boolean = false,
) {
    val context = LocalContext.current
    val language = LocalAppLanguage.current
    val player = remember(context) { AudioPlayer(context) }
    var playing by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var slow by rememberSaveable { mutableStateOf(false) }
    var droneOn by rememberSaveable { mutableStateOf(false) }
    var comparing by remember { mutableStateOf(false) }
    var audioError by remember { mutableStateOf(false) }
    var offline by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val question = session.question
    val speed = if (slow) SLOW_SPEED else 1f
    val hintDescription = stringResource(R.string.lesson_hint_cd)

    fun stopPhrasePlayback() {
        player.stopPhrase()
        playing = false
        loading = false
        progress = 0f
        comparing = false
    }

    fun playPhrase() {
        val source = question.audio
        if (source == null) {
            playing = false
            loading = false
            audioError = true
            offline = false
            return
        }
        if (isRemoteSource(source) && !hasNetwork(context)) {
            stopPhrasePlayback()
            offline = true
            audioError = true
            return
        }
        audioError = false
        offline = false
        comparing = false
        progress = 0f
        loading = true
        player.play(
            source = source,
            speed = speed,
            onStarted = {
                loading = false
                playing = true
            },
            onFinished = {
                playing = false
                loading = false
                progress = 1f
            },
            onInterrupted = {
                playing = false
                loading = false
                progress = 0f
            },
            onError = {
                playing = false
                loading = false
                audioError = true
                offline = isRemoteSource(source) && !hasNetwork(context)
            },
        )
    }

    fun playComparison() {
        val answerSource = question.audio
        val contrastSource = question.contrastAudio
        if (answerSource == null || contrastSource == null) {
            audioError = true
            return
        }
        val remote = isRemoteSource(answerSource) || isRemoteSource(contrastSource)
        if (remote && !hasNetwork(context)) {
            stopPhrasePlayback()
            offline = true
            audioError = true
            return
        }
        audioError = false
        offline = false
        progress = 0f
        comparing = true
        loading = true
        player.playSequence(
            sources = listOf(answerSource, contrastSource),
            speed = speed,
            onStarted = {
                loading = false
                playing = true
            },
            onFinished = {
                comparing = false
                playing = false
                loading = false
                progress = 0f
            },
            onInterrupted = {
                comparing = false
                playing = false
                loading = false
                progress = 0f
            },
            onError = {
                comparing = false
                playing = false
                loading = false
                audioError = true
                offline = remote && !hasNetwork(context)
            },
        )
    }

    LaunchedEffect(session.index) {
        scrollState.scrollTo(0)
        if (!session.answered) playPhrase()
    }

    LaunchedEffect(session.answered) {
        if (session.answered) {
            stopPhrasePlayback()
            withFrameNanos { }
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(playing, session.index) {
        while (playing) {
            val duration = player.durationMs()
            if (duration > 0) {
                progress = (player.positionMs().toFloat() / duration).coerceIn(0f, 1f)
            }
            delay(100)
        }
    }

    LaunchedEffect(droneOn, question.droneAudio) {
        if (!droneOn) {
            player.stopDrone()
            return@LaunchedEffect
        }
        val path = question.droneAudio
        if (path == null) {
            droneOn = false
            return@LaunchedEffect
        }
        player.startDrone(path, DRONE_VOLUME) { droneOn = false }
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
                    active = slow,
                    onToggle = {
                        slow = !slow
                        player.setSpeed(if (slow) SLOW_SPEED else 1f)
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
                        if (playing) stopPhrasePlayback() else playPhrase()
                    },
                )
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(
                        when {
                            comparing -> R.string.lesson_compare_caption
                            loading -> R.string.lesson_loading_audio
                            playing -> R.string.lesson_playing
                            else -> R.string.lesson_listen
                        },
                        question.answer.name(language),
                        question.contrast.name(language),
                    ),
                    color = if (comparing) NightGold else NightTextMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (question.droneAudio != null) {
                    DroneToggle(active = droneOn, onToggle = { droneOn = !droneOn })
                }
                if (question.audio != null && question.contrastAudio != null) {
                    TextButton(onClick = { playComparison() }) {
                        Text(
                            text = stringResource(R.string.lesson_compare),
                            color = NightGold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                if (RecognitionEngine.hintAvailable(session)) {
                    TextButton(
                        onClick = onHint,
                        modifier = Modifier.semantics {
                            contentDescription = hintDescription
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.lesson_hint),
                            color = NightGold,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            if (hintUnavailable) {
                Text(
                    text = stringResource(R.string.lesson_hint_unavailable),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                question.options.forEachIndexed { index, option ->
                    val state = answerState(
                        optionId = option.id,
                        selectedId = session.selectedId,
                        answerId = question.answer.id,
                    )
                    AnswerCard(
                        label = option.name(language),
                        state = state,
                        enabled = !session.answered && option.id != session.eliminatedId,
                        onClick = { onAnswer(option.id) },
                        contentDescription = stringResource(
                            R.string.lesson_answer_cd,
                            option.name(language),
                            index + 1,
                            question.options.size,
                        ),
                        modifier = if (option.id == session.eliminatedId) {
                            Modifier.alpha(0.4f)
                        } else {
                            Modifier
                        },
                    )
                }
            }
            if (session.answered) {
                FeedbackPanel(
                    correct = session.answeredCorrectly,
                    answerName = question.answer.name(language),
                    explanation = question.explanation(language),
                    accent = maqamColor(question.answer.id, question.answer.color),
                    track = question.track,
                    notationUrl = question.notationUrl,
                    sourceUrl = question.answer.sourceUrl,
                )
                PrimaryActionButton(
                    label = stringResource(R.string.lesson_next),
                    onClick = onNext,
                )
            } else if (audioError) {
                AudioErrorPanel(
                    message = if (offline) stringResource(R.string.lesson_audio_offline) else null,
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
