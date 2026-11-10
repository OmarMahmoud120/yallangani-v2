package com.osmartapps.yallanghanee.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.game.LessonResult
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.name
import com.osmartapps.yallanghanee.model.signature
import com.osmartapps.yallanghanee.model.title
import com.osmartapps.yallanghanee.ui.components.MaqamSigil
import com.osmartapps.yallanghanee.ui.components.MasteryRing
import com.osmartapps.yallanghanee.ui.components.PrimaryActionButton
import com.osmartapps.yallanghanee.ui.theme.NightBg
import com.osmartapps.yallanghanee.ui.theme.NightGold
import com.osmartapps.yallanghanee.ui.theme.NightOutlineSoft
import com.osmartapps.yallanghanee.ui.theme.NightSurface
import com.osmartapps.yallanghanee.ui.theme.NightSurfaceHigh
import com.osmartapps.yallanghanee.ui.theme.NightSuccess
import com.osmartapps.yallanghanee.ui.theme.NightText
import com.osmartapps.yallanghanee.ui.theme.NightTextMuted
import com.osmartapps.yallanghanee.ui.theme.maqamColor

@Composable
fun ResultsScreen(
    result: LessonResult,
    pack: MaqamPack,
    hasNextLesson: Boolean,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit,
) {
    BackHandler(onBack = onHome)
    val language = LocalAppLanguage.current
    val lesson = pack.lessons.firstOrNull { it.id == result.lessonId }
    val learned = lesson?.maqamIds?.mapNotNull { pack.maqamById(it) }.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NightBg, NightSurface)))
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.results_title),
            color = NightTextMuted,
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = stringResource(R.string.results_score, result.correct, result.total),
            color = NightGold,
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = stringResource(
                if (result.passed) R.string.results_passed else R.string.results_failed,
            ),
            color = if (result.passed) NightSuccess else NightText,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        if (lesson != null) {
            Text(
                text = lesson.title(language),
                color = NightTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (learned.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(NightSurfaceHigh)
                    .border(1.dp, NightOutlineSoft, RoundedCornerShape(24.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.results_learned),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
                learned.forEach { maqam ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        MaqamSigil(
                            name = maqam.name(language),
                            color = maqamColor(maqam.id, maqam.color),
                            size = 40.dp,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = maqam.name(language),
                                color = NightText,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                text = maqam.signature(language),
                                color = NightTextMuted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        MasteryRing(completed = result.passed)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        if (result.passed && hasNextLesson) {
            PrimaryActionButton(
                label = stringResource(R.string.results_next),
                onClick = onNext,
            )
            TextButton(onClick = onHome) {
                Text(
                    text = stringResource(R.string.results_home),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        } else if (result.passed) {
            PrimaryActionButton(
                label = stringResource(R.string.results_home),
                onClick = onHome,
            )
        } else {
            PrimaryActionButton(
                label = stringResource(R.string.results_retry),
                onClick = onRetry,
            )
            TextButton(onClick = onHome) {
                Text(
                    text = stringResource(R.string.results_home),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

