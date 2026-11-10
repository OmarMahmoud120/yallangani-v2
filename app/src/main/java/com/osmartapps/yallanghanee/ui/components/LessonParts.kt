package com.osmartapps.yallanghanee.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.model.Track
import com.osmartapps.yallanghanee.ui.theme.NightBg
import com.osmartapps.yallanghanee.ui.theme.NightError
import com.osmartapps.yallanghanee.ui.theme.NightGold
import com.osmartapps.yallanghanee.ui.theme.NightOutlineSoft
import com.osmartapps.yallanghanee.ui.theme.NightSurfaceHigh
import com.osmartapps.yallanghanee.ui.theme.NightSuccess
import com.osmartapps.yallanghanee.ui.theme.NightText
import com.osmartapps.yallanghanee.ui.theme.NightTextMuted

@Composable
fun PrimaryActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = NightGold,
            contentColor = NightBg,
        ),
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SlowToggle(active: Boolean, onToggle: () -> Unit) {
    TextButton(onClick = onToggle) {
        Text(
            text = stringResource(
                if (active) R.string.lesson_slow_on else R.string.lesson_slow,
            ),
            color = if (active) NightGold else NightTextMuted,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun DroneToggle(active: Boolean, onToggle: () -> Unit) {
    TextButton(onClick = onToggle) {
        Text(
            text = stringResource(
                if (active) R.string.lesson_drone_on else R.string.lesson_drone,
            ),
            color = if (active) NightGold else NightTextMuted,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
fun FeedbackPanel(
    correct: Boolean,
    answerName: String,
    explanation: String,
    accent: Color,
    track: Track? = null,
    notationUrl: String? = null,
    sourceUrl: String? = null,
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
        NotationImage(
            url = notationUrl,
            contentDescription = stringResource(R.string.lesson_notation_cd, answerName),
            modifier = Modifier.padding(top = 4.dp),
        )
        if (track != null) {
            Text(
                text = stringResource(R.string.lesson_track, track.title, track.artist),
                color = NightTextMuted,
                style = MaterialTheme.typography.labelMedium,
            )
            track.credits.forEach { credit ->
                Text(
                    text = stringResource(R.string.lesson_credits, credit),
                    color = NightTextMuted.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        if (sourceUrl != null) {
            val uriHandler = LocalUriHandler.current
            TextButton(
                onClick = { runCatching { uriHandler.openUri(sourceUrl) } },
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = stringResource(R.string.lesson_source),
                    color = NightGold,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
fun AudioErrorPanel(message: String? = null, onRetry: () -> Unit, onSkip: () -> Unit) {
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
            text = message ?: stringResource(R.string.lesson_audio_error),
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
fun ExitLessonDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
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
