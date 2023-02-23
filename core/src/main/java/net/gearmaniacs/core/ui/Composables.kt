package net.gearmaniacs.core.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import net.gearmaniacs.core.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    maxLength: Int,
) {
    val isError = value.isNotEmpty() && value.toIntOrNull() == null

    OutlinedTextField(
        modifier = modifier.onFocusChanged {
            it.hasFocus
        },
        value = value,
        onValueChange = {
            if (it.length <= maxLength) onValueChange(it)
        },
        singleLine = true,
        label = { Text(hint) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
        ),
        trailingIcon = {
            if (isError) Icon(
                painterResource(R.drawable.ic_error_outline),
                "error",
                tint = MaterialTheme.colorScheme.error
            )
        },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(stringResource(R.string.error_invalid_number))
            }
        }
    )
}
