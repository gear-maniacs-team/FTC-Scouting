package net.gearmaniacs.tournament.ui.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import net.gearmaniacs.tournament.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTournamentDialog(
    onDismiss: () -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    confirmAction: () -> Unit,
    confirmButtonText: String,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        properties = DialogProperties(decorFitsSystemWindows = false),
        title = { Text(stringResource(R.string.title_tournament_name)) },
        text = {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(stringResource(R.string.name)) },
                singleLine = true,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = confirmAction) {
                Text(confirmButtonText)
            }
        }
    )
}
