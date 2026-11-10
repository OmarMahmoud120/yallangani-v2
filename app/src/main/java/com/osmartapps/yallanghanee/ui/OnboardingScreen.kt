package com.osmartapps.yallanghanee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.audio.AudioPlayer
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.explanation
import com.osmartapps.yallanghanee.model.label
import com.osmartapps.yallanghanee.model.name
import com.osmartapps.yallanghanee.model.prompt
import com.osmartapps.yallanghanee.ui.components.AnswerCard
import com.osmartapps.yallanghanee.ui.components.AudioErrorPanel
import com.osmartapps.yallanghanee.ui.components.FeedbackPanel
import com.osmartapps.yallanghanee.ui.components.ListenOrb
import com.osmartapps.yallanghanee.ui.components.PrimaryActionButton
import com.osmartapps.yallanghanee.ui.components.ProgressDots
import com.osmartapps.yallanghanee.ui.components.answerState
import com.osmartapps.yallanghanee.ui.theme.NightBg
import com.osmartapps.yallanghanee.ui.theme.NightGold
import com.osmartapps.yallanghanee.ui.theme.NightSurface
import com.osmartapps.yallanghanee.ui.theme.NightText
import com.osmartapps.yallanghanee.ui.theme.NightTextMuted
import com.osmartapps.yallanghanee.ui.theme.maqamColor
import kotlinx.coroutines.delay

private const val AUDIO_DIR = "content/audio"

@Composable
fun OnboardingScreen(
    pack: MaqamPack,
    step: Int,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current
    val player = remember(context) { AudioPlayer(context) }
    val language = LocalAppLanguage.current
    val onboarding = pack.onboarding
    val steps = onboarding.steps
    val index = step.coerceIn(0, steps.size)
    val firstWin = index == steps.size
    val current = steps.getOrNull(index)
    val stageKey: Any = if (firstWin) "first_win" else index

    var selected by rememberSaveable(stageKey) { mutableStateOf<String?>(null) }
    var playing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var audioError by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val prompt = if (firstWin) onboarding.firstWin.prompt(language) else current!!.prompt(language)
    val audio = if (firstWin) onboarding.firstWin.audio else current!!.audio
    val answerId = if (firstWin) onboarding.firstWin.answer else current!!.answer
    val explanation = if (firstWin) {
        onboarding.firstWin.explanation(language)
    } else {
        current!!.explanation(language)
    }
    val options: List<Pair<String, String>> = if (firstWin) {
        onboarding.firstWin.maqamIds.map { id -> id to (pack.maqamById(id)?.name(language) ?: id) }
    } else {
        current!!.options.map { it.id to it.label(language) }
    }
    val answerLabel = options.first { it.first == answerId }.second
    val accent = if (firstWin) maqamColor(answerId) else NightGold

    fun stopPlayback() {
        player.stop()
        playing = false
        progress = 0f
    }

    fun playExercise() {
        audioError = false
        progress = 0f
        player.play(
            source = "$AUDIO_DIR/$audio",
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
        playing = player.isActive
    }

    LaunchedEffect(stageKey) {
        stopPlayback()
        audioError = false
        scrollState.scrollTo(0)
    }

    LaunchedEffect(selected) {
        if (selected != null) {
            withFrameNanos { }
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(playing, stageKey) {
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
                .padding(horizontal = 24.dp, vertical = if (compact) 10.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ProgressDots(
                    current = if (firstWin) steps.size - 1 else index,
                    total = steps.size,
                    modifier = Modifier.align(Alignment.Center),
                )
                TextButton(
                    onClick = onFinish,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        color = NightTextMuted,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Text(
                text = stringResource(
                    if (firstWin) R.string.onboarding_challenge else R.string.onboarding_eyebrow,
                ),
                color = NightGold,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = prompt,
                color = NightText,
                style = if (compact) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.headlineMedium
                },
                modifier = Modifier.semantics { heading() },
            )
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ListenOrb(
                    isPlaying = playing,
                    progress = progress,
                    enabled = true,
                    diameter = if (compact) 100.dp else 116.dp,
                    onClick = {
                        if (playing) stopPlayback() else playExercise()
                    },
                )
            }
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(
                        when {
                            playing -> R.string.lesson_playing
                            progress >= 1f -> R.string.onboarding_replay_hint
                            else -> R.string.onboarding_listen_hint
                        },
                    ),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                options.forEachIndexed { optionIndex, option ->
                    AnswerCard(
                        label = option.second,
                        state = answerState(
                            optionId = option.first,
                            selectedId = selected,
                            answerId = answerId,
                        ),
                        enabled = selected == null,
                        onClick = { selected = option.first },
                        contentDescription = stringResource(
                            R.string.lesson_answer_cd,
                            option.second,
                            optionIndex + 1,
                            options.size,
                        ),
                    )
                }
            }
            if (selected != null) {
                FeedbackPanel(
                    correct = selected == answerId,
                    answerName = answerLabel,
                    explanation = explanation,
                    accent = accent,
                )
                PrimaryActionButton(
                    label = stringResource(
                        if (firstWin) R.string.onboarding_start else R.string.onboarding_next,
                    ),
                    onClick = if (firstWin) onFinish else onNext,
                )
            } else if (audioError) {
                AudioErrorPanel(
                    onRetry = { playExercise() },
                    onSkip = if (firstWin) onFinish else onNext,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
