package com.osmartapps.yallanghanee.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.model.MotionPreference
import com.osmartapps.yallanghanee.ui.theme.NightBg
import com.osmartapps.yallanghanee.ui.theme.NightError
import com.osmartapps.yallanghanee.ui.theme.NightGold
import com.osmartapps.yallanghanee.ui.theme.NightOutline
import com.osmartapps.yallanghanee.ui.theme.NightSurface
import com.osmartapps.yallanghanee.ui.theme.NightSuccess
import com.osmartapps.yallanghanee.ui.theme.NightText
import com.osmartapps.yallanghanee.ui.theme.NightTextMuted

@Composable
fun MaqamSigil(
    name: String,
    color: Color,
    size: Dp = 44.dp,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.75f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = sigilLetter(name),
            color = color,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

internal fun sigilLetter(name: String): String =
    name.firstOrNull { it.isLetterOrDigit() }?.toString() ?: name.take(1)

@Composable
fun ListenOrb(
    isPlaying: Boolean,
    progress: Float,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    diameter: Dp = 132.dp,
) {
    val context = LocalContext.current
    val motionEnabled = rememberMotionEnabled(context)
    val transition = rememberInfiniteTransition(label = "orb")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "orbPulse",
    )
    val scale = if (isPlaying && motionEnabled) pulse else 1f
    val description = stringResource(
        if (isPlaying) R.string.lesson_stop else R.string.lesson_listen,
    )

    Box(
        modifier = modifier
            .size(diameter)
            .scale(scale)
            .semantics {
                contentDescription = description
                role = Role.Button
            }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(diameter - 8.dp)) {
            val stroke = 3.dp.toPx()
            drawArc(
                color = NightGold.copy(alpha = 0.20f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(stroke / 2, stroke / 2),
                size = androidx.compose.ui.geometry.Size(
                    size.width - stroke,
                    size.height - stroke,
                ),
                style = Stroke(stroke, cap = StrokeCap.Round),
            )
            if (progress > 0f) {
                drawArc(
                    color = NightGold,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = androidx.compose.ui.geometry.Size(
                        size.width - stroke,
                        size.height - stroke,
                    ),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        Box(
            modifier = Modifier
                .size(diameter - 32.dp)
                .clip(CircleShape)
                .background(if (enabled) NightGold else NightGold.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = NightBg,
                modifier = Modifier.size(44.dp),
            )
        }
    }
}

enum class AnswerState { Idle, Correct, Incorrect, Revealed }

fun answerState(optionId: String, selectedId: String?, answerId: String): AnswerState = when {
    selectedId == null -> AnswerState.Idle
    optionId == selectedId && optionId == answerId -> AnswerState.Correct
    optionId == selectedId -> AnswerState.Incorrect
    optionId == answerId -> AnswerState.Revealed
    else -> AnswerState.Idle
}

@Composable
fun AnswerCard(
    label: String,
    state: AnswerState,
    enabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val border = when (state) {
        AnswerState.Correct -> NightSuccess
        AnswerState.Incorrect -> NightError
        AnswerState.Revealed -> NightSuccess.copy(alpha = 0.7f)
        AnswerState.Idle -> NightOutline
    }
    val background = when (state) {
        AnswerState.Correct -> NightSuccess.copy(alpha = 0.16f)
        AnswerState.Incorrect -> NightError.copy(alpha = 0.16f)
        AnswerState.Revealed -> NightSuccess.copy(alpha = 0.08f)
        AnswerState.Idle -> NightSurface
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .border(if (state == AnswerState.Idle) 1.dp else 2.dp, border, RoundedCornerShape(20.dp))
            .semantics { this.contentDescription = contentDescription }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = NightText,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ProgressDots(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.lesson_progress_cd, current + 1, total)
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == current) 20.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            index < current -> NightGold.copy(alpha = 0.55f)
                            index == current -> NightGold
                            else -> NightTextMuted.copy(alpha = 0.30f)
                        },
                    ),
            )
        }
    }
}

@Composable
fun MasteryRing(
    completed: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (completed) NightSuccess.copy(alpha = 0.18f) else Color.Transparent)
            .border(
                1.dp,
                if (completed) NightSuccess else NightTextMuted.copy(alpha = 0.35f),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (completed) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = NightSuccess,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

val LocalMotionPreference = staticCompositionLocalOf { MotionPreference.System }

@Composable
fun rememberMotionEnabled(context: android.content.Context): Boolean {
    val preference = LocalMotionPreference.current
    val systemAnimationsEnabled = remember(context) {
        android.provider.Settings.Global.getFloat(
            context.contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) > 0f
    }
    return preference.enabled(systemAnimationsEnabled)
}
