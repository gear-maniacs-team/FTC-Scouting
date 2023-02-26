package net.gearmaniacs.login.ui.screen

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.login.R
import net.gearmaniacs.login.ui.activity.IntroActivity
import net.gearmaniacs.login.viewmodel.LoginViewModel

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val ctx = LocalContext.current
        val viewModel: LoginViewModel = viewModel()

        val seenIntro by viewModel.seenIntroFlow.collectAsState(null)

        LaunchedEffect(seenIntro) {
            if (seenIntro == false) {
                ctx.startActivity(Intent(ctx, IntroActivity::class.java))
            }

            if (Firebase.isLoggedIn) {
                // If the user is logged in go back to MainActivity
                navigator.pop()
            }
        }

        Menu()
    }

    @Composable
    private fun Menu() {
        val viewModel: LoginViewModel = viewModel()
        val nav = LocalNavigator.currentOrThrow

        val backgroundColor = colorResource(R.color.colorPrimary)
        var showOfflineDialog by remember { mutableStateOf(false) }

        Column(
            Modifier
                .fillMaxHeight()
                .background(backgroundColor)
                .verticalScroll(rememberScrollState())
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painterResource(R.drawable.app_background),
                stringResource(R.string.app_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .sizeIn(maxHeight = 182.dp)
                    .padding(20.dp),
            )

            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.weight(1f))

            Column(
                modifier = Modifier.padding(horizontal = 42.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { nav.push(SignInTab()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = backgroundColor
                    )
                ) {
                    Text(stringResource(R.string.action_sign_in), fontSize = 17.sp)
                }

                OutlinedButton(
                    onClick = { nav.push(RegisterTab()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color.White,
                    ),
                ) {
                    Text(stringResource(R.string.action_sign_up), fontSize = 17.sp)
                }

                Button(
                    onClick = { showOfflineDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = backgroundColor
                    ),
                ) {
                    Text(stringResource(R.string.action_use_without_account), fontSize = 17.sp)
                }
            }
        }

        if (showOfflineDialog) {
            val scope = rememberCoroutineScope()

            AlertDialog(
                onDismissRequest = { showOfflineDialog = false },
                title = { Text(stringResource(R.string.confirm_use_offline_account)) },
                text = { Text(stringResource(R.string.confirm_use_offline_account_desc)) },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            viewModel.useOfflineAccount()
                            nav.pop()
                        }
                    }) {
                        Text(stringResource(R.string.action_use_offline))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showOfflineDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
            )
        }
    }
}