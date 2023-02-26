package net.gearmaniacs.login.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import net.gearmaniacs.login.R

@Composable
internal fun EmailTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true,
        label = { Text(stringResource(R.string.prompt_email)) },
        leadingIcon = {
            Icon(
                painterResource(R.drawable.ic_mail_outline),
                stringResource(R.string.prompt_email)
            )
        },
        supportingText = {
            if (isError) {
                Text(stringResource(R.string.error_invalid_email))
            }
        },
        isError = isError,
    )
}

@Composable
internal fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorMessage: String = stringResource(R.string.error_invalid_password),
    hint: String = stringResource(R.string.prompt_password),
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true,
        label = { Text(hint) },
        leadingIcon = {
            Icon(
                painterResource(R.drawable.ic_outline_lock),
                stringResource(R.string.prompt_email)
            )
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                val icon =
                    if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility_on

                Icon(painterResource(icon), null)
            }
        },
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        supportingText = {
            if (isError) {
                Text(errorMessage)
            }
        },
        isError = isError,
    )
}