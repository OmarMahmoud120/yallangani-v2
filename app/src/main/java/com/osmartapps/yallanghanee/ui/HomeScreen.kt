package com.osmartapps.yallanghanee.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.model.Maqam
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.RecognitionLesson
import com.osmartapps.yallanghanee.ui.components.MaqamSigil
import com.osmartapps.yallanghanee.ui.components.MasteryRing
import com.osmartapps.yallanghanee.ui.theme.NightBg
import com.osmartapps.yallanghanee.ui.theme.NightGold
import com.osmartapps.yallanghanee.ui.theme.NightOutlineSoft
import com.osmartapps.yallanghanee.ui.theme.NightSurface
import com.osmartapps.yallanghanee.ui.theme.NightSurfaceHigh
import com.osmartapps.yallanghanee.ui.theme.NightText
import com.osmartapps.yallanghanee.ui.theme.NightTextMuted
import com.osmartapps.yallanghanee.ui.theme.maqamColor

@Composable
fun HomeScreen(
    pack: MaqamPack,
    completedLessons: Set<String>,
    onStartLesson: (String) -> Unit,
    onResetProgress: () -> Unit,
) {
    var showResetDialog by remember { mutableStateOf(false) }
    val completedCount = pack.lessons.count { it.id in completedLessons }
    val nextLesson = pack.lessons.firstOrNull { it.id !in completedLessons }
    val masteredMaqams = remember(pack, completedLessons) { masteredMaqams(pack, completedLessons) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(NightBg, NightSurface, NightBg),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            HomeHeader()
            Text(
                text = stringResource(R.string.home_intro_body),
                color = NightTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            ContinueCard(
                lesson = nextLesson ?: pack.lessons.lastOrNull(),
                pack = pack,
                completedAll = nextLesson == null,
                hasProgress = completedCount > 0,
                onStart = {
                    val target = nextLesson ?: pack.lessons.lastOrNull()
                    if (target != null) onStartLesson(target.id)
                },
            )
            Text(
                text = stringResource(R.string.home_path),
                color = NightTextMuted,
                style = MaterialTheme.typography.titleMedium,
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                pack.maqams.forEach { maqam ->
                    val lesson = pack.lessons.firstOrNull { maqam.id in it.maqamIds }
                    MaqamRow(
                        maqam = maqam,
                        mastered = maqam.id in masteredMaqams,
                        enabled = lesson != null,
                        onClick = { lesson?.let { onStartLesson(it.id) } },
                    )
                }
            }
            ProgressFooter(
                completed = completedCount,
                total = pack.lessons.size,
            )
            pack.notice?.let { notice ->
                Text(
                    text = notice,
                    color = NightTextMuted.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(
                    text = stringResource(R.string.home_reset),
                    color = NightTextMuted.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelMedium,
                )
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
    }
}

@Composable
private fun HomeHeader() {
    Column {
        Text(
            text = stringResource(R.string.app_name),
            color = NightGold,
            style = MaterialTheme.typography.displayMedium,
        )
        Text(
            text = stringResource(R.string.product_line),
            color = NightTextMuted,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun ContinueCard(
    lesson: RecognitionLesson?,
    pack: MaqamPack,
    completedAll: Boolean,
    hasProgress: Boolean,
    onStart: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(NightSurfaceHigh)
            .border(1.dp, NightOutlineSoft, RoundedCornerShape(28.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.home_intro_title),
            color = NightTextMuted,
            style = MaterialTheme.typography.labelLarge,
        )
        if (completedAll) {
            Text(
                text = stringResource(R.string.home_completed),
                color = NightGold,
                style = MaterialTheme.typography.headlineSmall,
            )
        } else if (lesson != null) {
            Text(
                text = stringResource(R.string.home_next_review, lesson.titleAr),
                color = NightText,
                style = MaterialTheme.typography.headlineSmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                lesson.maqamIds.mapNotNull { pack.maqamById(it) }.forEach { maqam ->
                    MaqamSigil(
                        nameAr = maqam.nameAr,
                        color = maqamColor(maqam.id),
                        size = 38.dp,
                    )
                }
            }
        }
        Button(
            onClick = onStart,
            enabled = lesson != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NightGold,
                contentColor = NightBg,
            ),
        ) {
            Text(
                text = stringResource(
                    when {
                        completedAll -> R.string.results_retry
                        hasProgress -> R.string.home_continue
                        else -> R.string.home_start
                    },
                ),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun MaqamRow(
    maqam: Maqam,
    mastered: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val masteryLabel = stringResource(
        if (mastered) R.string.maqam_done else R.string.maqam_new,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(NightSurface)
            .border(1.dp, NightOutlineSoft, RoundedCornerShape(20.dp))
            .semantics {
                contentDescription = "${maqam.nameAr}, ${maqam.jinsAr}, $masteryLabel"
            }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        MaqamSigil(
            nameAr = maqam.nameAr,
            color = maqamColor(maqam.id),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = maqam.nameAr,
                color = NightText,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = maqam.jinsAr,
                color = NightTextMuted,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        if (mastered) {
            Text(
                text = masteryLabel,
                color = NightGold,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        MasteryRing(completed = mastered)
    }
}

@Composable
private fun ProgressFooter(completed: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.home_progress, completed, total),
            color = NightTextMuted,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = "·",
            color = NightTextMuted.copy(alpha = 0.5f),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = stringResource(R.string.home_offline),
            color = NightTextMuted,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun ResetProgressDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.reset_title),
                color = NightText,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.reset_message),
                color = NightTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.reset_confirm),
                    color = NightGold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.reset_cancel),
                    color = NightTextMuted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        containerColor = NightSurfaceHigh,
    )
}

private fun masteredMaqams(pack: MaqamPack, completed: Set<String>): Set<String> =
    pack.maqams
        .filter { maqam ->
            val related = pack.lessons.filter { maqam.id in it.maqamIds }
            related.isNotEmpty() && related.all { it.id in completed }
        }
        .map { it.id }
        .toSet()
