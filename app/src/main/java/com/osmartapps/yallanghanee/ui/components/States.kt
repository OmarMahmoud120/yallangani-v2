package com.osmartapps.yallanghanee.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.osmartapps.yallanghanee.R
import com.osmartapps.yallanghanee.ui.theme.NightBg
import com.osmartapps.yallanghanee.ui.theme.NightGold
import com.osmartapps.yallanghanee.ui.theme.NightText
import com.osmartapps.yallanghanee.ui.theme.NightTextMuted

@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CircularProgressIndicator(color = NightGold)
            Text(
                text = stringResource(R.string.loading),
                color = NightTextMuted,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
fun ErrorState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                text = stringResource(R.string.load_error),
                color = NightText,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NightGold,
                    contentColor = NightBg,
                ),
            ) {
                Text(
                    text = stringResource(R.string.retry),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
