package com.osmartapps.yallanghanee.ui.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.ui.components.AnswerCard
import com.osmartapps.yallanghanee.ui.components.AnswerState
import com.osmartapps.yallanghanee.ui.components.AudioErrorPanel
import com.osmartapps.yallanghanee.ui.components.FeedbackPanel
import com.osmartapps.yallanghanee.ui.components.ListenOrb
import com.osmartapps.yallanghanee.ui.components.MaqamSigil
import com.osmartapps.yallanghanee.ui.components.MasteryRing
import com.osmartapps.yallanghanee.ui.components.ProgressDots
import com.osmartapps.yallanghanee.ui.components.SlowToggle
import com.osmartapps.yallanghanee.ui.theme.NightBg
import com.osmartapps.yallanghanee.ui.theme.NightGold
import com.osmartapps.yallanghanee.ui.theme.NightSurface
import com.osmartapps.yallanghanee.ui.theme.NightText
import com.osmartapps.yallanghanee.ui.theme.NightTextMuted
import com.osmartapps.yallanghanee.ui.theme.maqamColor

@Composable
fun GalleryScreen(pack: MaqamPack, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NightBg, NightSurface)))
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.gallery_title),
                color = NightGold,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onBack) {
                Text(
                    text = stringResource(R.string.gallery_back),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Section(R.string.gallery_sigils) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                pack.maqams.forEach { maqam ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        MaqamSigil(
                            nameAr = maqam.nameAr,
                            color = maqamColor(maqam.id),
                            size = 60.dp,
                        )
                        Text(
                            text = maqam.nameAr,
                            color = NightTextMuted,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }

        Section(R.string.gallery_orb) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OrbSample(R.string.gallery_orb_idle) {
                    ListenOrb(
                        isPlaying = false,
                        progress = 0f,
                        enabled = true,
                        onClick = {},
                        diameter = 64.dp,
                    )
                }
                OrbSample(R.string.gallery_orb_playing) {
                    ListenOrb(
                        isPlaying = true,
                        progress = 0.35f,
                        enabled = true,
                        onClick = {},
                        diameter = 64.dp,
                    )
                }
                OrbSample(R.string.gallery_orb_complete) {
                    ListenOrb(
                        isPlaying = true,
                        progress = 1f,
                        enabled = true,
                        onClick = {},
                        diameter = 64.dp,
                    )
                }
                OrbSample(R.string.gallery_orb_disabled) {
                    ListenOrb(
                        isPlaying = false,
                        progress = 0f,
                        enabled = false,
                        onClick = {},
                        diameter = 64.dp,
                    )
                }
            }
        }

        Section(R.string.gallery_answers) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AnswerCard(
                    label = stringResource(R.string.gallery_answer_idle),
                    state = AnswerState.Idle,
                    enabled = true,
                    onClick = {},
                    contentDescription = stringResource(R.string.gallery_answer_idle),
                )
                AnswerCard(
                    label = stringResource(R.string.gallery_answer_correct),
                    state = AnswerState.Correct,
                    enabled = false,
                    onClick = {},
                    contentDescription = stringResource(R.string.gallery_answer_correct),
                )
                AnswerCard(
                    label = stringResource(R.string.gallery_answer_incorrect),
                    state = AnswerState.Incorrect,
                    enabled = false,
                    onClick = {},
                    contentDescription = stringResource(R.string.gallery_answer_incorrect),
                )
                AnswerCard(
                    label = stringResource(R.string.gallery_answer_revealed),
                    state = AnswerState.Revealed,
                    enabled = false,
                    onClick = {},
                    contentDescription = stringResource(R.string.gallery_answer_revealed),
                )
            }
        }

        Section(R.string.gallery_progress) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ProgressDots(current = 2, total = 5)
                ProgressDots(current = 0, total = 5)
            }
        }

        Section(R.string.gallery_mastery) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MasteryRing(completed = false)
                Text(
                    text = stringResource(R.string.gallery_mastery_none),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
                MasteryRing(completed = true)
                Text(
                    text = stringResource(R.string.gallery_mastery_done),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        Section(R.string.gallery_controls) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SlowToggle(active = false, onToggle = {})
                SlowToggle(active = true, onToggle = {})
            }
        }

        Section(R.string.gallery_feedback) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeedbackPanel(
                    correct = true,
                    answerName = pack.maqams.first().nameAr,
                    explanation = pack.maqams.first().signatureAr,
                    accent = maqamColor(pack.maqams.first().id),
                )
                FeedbackPanel(
                    correct = false,
                    answerName = pack.maqams.last().nameAr,
                    explanation = pack.maqams.last().signatureAr,
                    accent = maqamColor(pack.maqams.last().id),
                )
            }
        }

        Section(R.string.gallery_audio_error) {
            AudioErrorPanel(onRetry = {}, onSkip = {})
        }

        Section(R.string.gallery_typography) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.gallery_type_display),
                    color = NightGold,
                    style = MaterialTheme.typography.displayMedium,
                )
                Text(
                    text = stringResource(R.string.gallery_type_headline),
                    color = NightText,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(R.string.gallery_type_body),
                    color = NightText,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.gallery_type_label),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun Section(title: Int, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(title),
            color = NightText,
            style = MaterialTheme.typography.titleMedium,
        )
        content()
    }
}

@Composable
private fun OrbSample(label: Int, orb: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        orb()
        Text(
            text = stringResource(label),
            color = NightTextMuted,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
