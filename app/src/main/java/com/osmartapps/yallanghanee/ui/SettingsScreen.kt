package com.osmartapps.yallanghanee.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.osmartapps.yallanghanee.BuildConfig
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.model.MaqamPack
import com.osmartapps.yallanghanee.model.MotionPreference
import com.osmartapps.yallanghanee.ui.theme.NightBg
import com.osmartapps.yallanghanee.ui.theme.NightError
import com.osmartapps.yallanghanee.ui.theme.NightGold
import com.osmartapps.yallanghanee.ui.theme.NightOutlineSoft
import com.osmartapps.yallanghanee.ui.theme.NightSurface
import com.osmartapps.yallanghanee.ui.theme.NightSurfaceHigh
import com.osmartapps.yallanghanee.ui.theme.NightText
import com.osmartapps.yallanghanee.ui.theme.NightTextMuted

@Composable
fun SettingsScreen(
    pack: MaqamPack,
    motionPreference: MotionPreference,
    onMotionChange: (MotionPreference) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NightBg, NightSurface)))
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_title),
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

        Text(
            text = stringResource(R.string.settings_motion),
            color = NightTextMuted,
            style = MaterialTheme.typography.titleMedium,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            MotionOption(
                label = stringResource(R.string.settings_motion_system),
                selected = motionPreference == MotionPreference.System,
                onSelect = { onMotionChange(MotionPreference.System) },
            )
            MotionOption(
                label = stringResource(R.string.settings_motion_reduced),
                selected = motionPreference == MotionPreference.Reduced,
                onSelect = { onMotionChange(MotionPreference.Reduced) },
            )
            MotionOption(
                label = stringResource(R.string.settings_motion_full),
                selected = motionPreference == MotionPreference.Full,
                onSelect = { onMotionChange(MotionPreference.Full) },
            )
        }
        Text(
            text = stringResource(R.string.settings_motion_hint),
            color = NightTextMuted,
            style = MaterialTheme.typography.labelMedium,
        )

        Text(
            text = stringResource(R.string.settings_about),
            color = NightTextMuted,
            style = MaterialTheme.typography.titleMedium,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(NightSurfaceHigh)
                .border(1.dp, NightOutlineSoft, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                color = NightText,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.settings_offline),
                color = NightTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            pack.notice?.let { notice ->
                Text(
                    text = notice,
                    color = NightTextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = stringResource(R.string.settings_audio_license),
                color = NightTextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.settings_fonts_license),
                color = NightTextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextButton(onClick = { showResetDialog = true }) {
                Text(
                    text = stringResource(R.string.settings_reset),
                    color = NightError,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(
                text = stringResource(R.string.settings_reset_hint),
                color = NightTextMuted.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
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
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetProgress()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.reset_confirm),
                        color = NightGold,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
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
}

@Composable
private fun MotionOption(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = NightGold,
                unselectedColor = NightTextMuted,
            ),
        )
        Text(
            text = label,
            color = if (selected) NightText else NightTextMuted,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
