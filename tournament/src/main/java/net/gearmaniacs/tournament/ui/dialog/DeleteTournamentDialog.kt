package net.gearmaniacs.tournament.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.google.firebase.ktx.Firebase
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.tournament.R

@Composable
fun DeleteTournamentDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val message =
        if (Firebase.isLoggedIn) R.string.delete_tournament_desc else R.string.delete_tournament_desc_offline

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(painterResource(R.drawable.ic_delete), null)
        },
        title = { Text(stringResource(R.string.delete_tournament)) },
        text = { Text(stringResource(message)) },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}