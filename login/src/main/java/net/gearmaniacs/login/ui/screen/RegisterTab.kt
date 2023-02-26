package net.gearmaniacs.login.ui.screen

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.extensions.toIntOrElse
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.ui.NumberField
import net.gearmaniacs.login.R
import net.gearmaniacs.login.ui.EmailTextField
import net.gearmaniacs.login.ui.PasswordTextField

internal class RegisterTab : Screen {

    var isLoading by mutableStateOf(false)

    @Composable
    override fun Content() {
        val nav = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
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
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                SignUp()
            }
        }
    }

    @Composable
    private fun ColumnScope.SignUp() {
        val nav = LocalNavigator.currentOrThrow

        var teamNumber by remember { mutableStateOf("") }
        var teamName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        val emailError by remember {
            derivedStateOf { email.isNotBlank() && !email.isValidEmail() }
        }
        val passwordError by remember {
            derivedStateOf { password.isNotBlank() && password.length < 6 }
        }
        val confirmPasswordError by remember {
            derivedStateOf { password != confirmPassword }
        }

        NumberField(
            modifier = Modifier.fillMaxWidth(),
            value = teamNumber,
            onValueChange = { teamNumber = it },
            hint = stringResource(R.string.prompt_team_number),
            maxLength = 7,
        )

        OutlinedTextField(
            value = teamName,
            onValueChange = { teamName = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.prompt_team_name)) }
        )

        Text(
            "Team Number and Name can be changed later",
            fontSize = 14.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
        )

        EmailTextField(email, { email = it }, emailError)
        PasswordTextField(password, { password = it }, passwordError)
        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            isError = confirmPasswordError,
            errorMessage = stringResource(R.string.error_incorrect_confirm_password),
            hint = stringResource(R.string.prompt_confirm_password)
        )

        Spacer(Modifier.height(8.dp))

        val ctx = LocalContext.current as Activity
        Button(
            onClick = {
                if (teamNumber.toIntOrElse(-1) == -1 || emailError || passwordError || confirmPasswordError)
                    return@Button

                registerEmail(ctx, UserTeam(teamNumber.toInt(), teamName), email, password) {
                    nav.popUntilRoot()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(R.string.action_register_team_account).uppercase(),
                fontSize = 18.sp
            )
        }

        OutlinedButton(
            onClick = { nav.replace(SignInTab()) },
            modifier = Modifier
                .padding(top = 42.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.action_already_own_account))
        }
    }

    private fun registerEmail(
        activity: Activity,
        userTeam: UserTeam,
        email: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        isLoading = true

        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Log.d(key, "registerWithEmail:success")
                    registerUserInDatabase(userTeam, onSuccess)
                } else {
                    Log.w(key, "registerWithEmail:failure")
                    activity.longToast("Registration failed.")
                    isLoading = false
                }
            }
    }

    private fun registerUserInDatabase(userTeam: UserTeam, onSuccess: () -> Unit) {
        Firebase.database.getReference(DatabasePaths.KEY_USERS)
            .child(Firebase.auth.currentUser!!.uid).setValue(userTeam) { error, _ ->
                if (error == null) {
                    Log.d(key, "registerInDatabase:success")
                    onSuccess()
                } else {
                    Log.e(key, "registerInDatabase:failure")
                }
                isLoading = false
            }
    }
}