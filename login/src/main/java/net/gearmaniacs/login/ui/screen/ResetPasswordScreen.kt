package net.gearmaniacs.login.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.login.R

internal class ResetPasswordScreen : Screen {

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow
        val ctx = LocalContext.current
        var isLoading by remember { mutableStateOf(false) }

        Scaffold(bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = { nav.pop() }) {
                        Icon(painterResource(R.drawable.ic_arrow_back), null)
                    }
                }
            )
        }) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                var email by remember { mutableStateOf("") }

                val emailError by remember {
                    derivedStateOf {
                        email.isNotBlank() && !email.isValidEmail()
                    }
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    label = { Text(stringResource(R.string.prompt_email)) },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_mail_outline),
                            stringResource(R.string.prompt_email)
                        )
                    },
                    supportingText = {
                        if (emailError) {
                            Text(stringResource(R.string.error_invalid_email))
                        }
                    },
                    isError = emailError,
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (!emailError) {
                            isLoading = true
                            Firebase.auth.sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    isLoading = false

                                    if (task.isSuccessful) {
                                        Log.d(key, "Password Reset Email sent.")
                                        ctx.longToast(R.string.reset_password_email_sent)
                                    } else {
                                        Log.w(key, "Error sending Password Reset Email")
                                        ctx.longToast(R.string.reset_password_email_not_sent)
                                    }
                                }
                        }
                    }
                ) {
                    Text(stringResource(R.string.action_reset_password))
                }
            }
        }
    }
}