package net.gearmaniacs.login.ui.screen

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import net.gearmaniacs.login.ui.EmailTextField
import net.gearmaniacs.login.ui.PasswordTextField

internal class SignInTab : Screen {

    var isLoading by mutableStateOf(false)

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow

        Scaffold(topBar = {
            Box(Modifier.windowInsetsPadding(WindowInsets.statusBars)) {
                IconButton(onClick = { nav.pop() }) {
                    Icon(painterResource(R.drawable.ic_arrow_back), null)
                }

                Image(
                    painterResource(R.drawable.app_background),
                    null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(maxHeight = 172.dp)
                        .align(Alignment.Center)
                )
            }
        }) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                SignIn()
            }
        }
    }

    @Composable
    private fun SignIn() {
        val nav = LocalNavigator.currentOrThrow

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        val emailError by remember {
            derivedStateOf { email.isNotBlank() && !email.isValidEmail() }
        }
        val passwordError by remember {
            derivedStateOf { password.isNotBlank() && password.length < 6 }
        }

        EmailTextField(email, { email = it }, emailError)
        PasswordTextField(password, { password = it }, passwordError)

        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            TextButton(
                onClick = { nav.push(ResetPasswordScreen()) },
                modifier = Modifier.weight(0.4f),
            ) {
                Text(stringResource(R.string.action_forgot_password))
            }

            Spacer(Modifier.width(8.dp))

            val ctx = LocalContext.current as Activity
            Button(
                onClick = {
                    if (!emailError && !passwordError) {
                        isLoading = true
                        val context = ctx.applicationContext
                        Firebase.auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(ctx) { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    Log.d(key, "signInWithEmail:success")
                                    nav.popUntilRoot()
                                } else {
                                    Log.w(key, "signInWithEmail:failure", task.exception)
                                    context.longToast("Login failed")
                                }
                            }
                    }
                },
                modifier = Modifier.weight(0.6f),
                enabled = !isLoading,
            ) {
                Text(stringResource(R.string.action_sign_in))
            }
        }

        OutlinedButton(
            onClick = { nav.replace(RegisterTab()) },
            modifier = Modifier.padding(top = 42.dp),
            enabled = !isLoading,
        ) {
            Text(stringResource(R.string.action_no_account))
        }
    }
}
