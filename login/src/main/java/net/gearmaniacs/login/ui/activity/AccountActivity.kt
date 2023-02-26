package net.gearmaniacs.login.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.extensions.toIntOrElse
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseConstants
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.model.isNullOrEmpty
import net.gearmaniacs.core.ui.NumberField
import net.gearmaniacs.core.ui.theme.AppTheme
import net.gearmaniacs.database.SignOutCleaner
import net.gearmaniacs.login.R
import net.gearmaniacs.login.viewmodel.AccountViewModel
import javax.inject.Inject

@AndroidEntryPoint
class AccountActivity : ComponentActivity() {

    private val viewModel by viewModels<AccountViewModel>()

    @Inject
    lateinit var signOutCleaner: Lazy<SignOutCleaner>

    private var linkedWithGoogle by mutableStateOf(false)

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val context = applicationContext
            lifecycleScope.launch(Dispatchers.Main.immediate) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

                try {
                    val account = task.getResult(ApiException::class.java)!!

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    val linkTask = Firebase.auth.currentUser!!.linkWithCredential(credential)
                    linkTask.await()
                    updateLinkedProviders()

                    if (linkTask.isSuccessful)
                        context.toast(R.string.account_provider_google_link_success)
                    else
                        throw IllegalStateException("Could not link account with Google")
                } catch (e: Exception) {
                    Log.w(TAG, "Google Account linking failed", e)
                    context.longToast(R.string.account_provider_google_link_failure)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppTheme {
                Scaffold(bottomBar = {
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        actions = {
                            IconButton(onClick = { finish() }) {
                                Icon(painterResource(R.drawable.ic_arrow_back), null)
                            }
                        }
                    )
                }) { paddingValues ->
                    Box(Modifier.padding(paddingValues)) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            val userTeam by viewModel.userTeamFlow.collectAsState(UserTeam())

                            AccountCategory(userTeam)
                            Spacer(Modifier.height(16.dp))
                            TeamDetailsCategory(userTeam)
                        }
                    }
                }

            }
        }

        val originalUserData = intent.getParcelableExtra<UserTeam>(ARG_USER_TEAM)

        if (originalUserData.isNullOrEmpty()) {
            Toast.makeText(this, R.string.team_details_previous_not_found, Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        updateLinkedProviders()
    }

    @Composable
    private fun AccountCategory(userTeam: UserTeam) {
        val user = Firebase.auth.currentUser ?: return

        var layoutEnabled by remember { mutableStateOf(true) }
        var showSignOutDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }

        Text(
            stringResource(R.string.title_account),
            fontSize = 19.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(Modifier.height(4.dp))

        Text(user.displayName ?: userTeam.teamName)
        user.email?.let {
            Text(it, fontSize = 14.sp)
        }

        Row(Modifier.fillMaxWidth()) {
            val modifier = Modifier
                .weight(1f)
                .padding(8.dp)
            Button(
                onClick = { showSignOutDialog = true },
                modifier = modifier,
                enabled = layoutEnabled,
            ) {
                Text(stringResource(R.string.action_sign_out))
            }

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = modifier,
                enabled = layoutEnabled,
            ) {
                Text(stringResource(R.string.action_delete_account))
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(R.string.link_your_account),
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Google", modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    if (linkedWithGoogle) {
                        unlinkFromGoogle()
                    } else {
                        linkWithGoogle()
                    }
                },
                enabled = layoutEnabled,
            ) {
                Text(stringResource(if (linkedWithGoogle) R.string.action_disconnect else R.string.action_connect))
            }
        }

        if (showSignOutDialog) {
            AlertDialog(
                onDismissRequest = { showSignOutDialog = false },
                title = { Text(stringResource(R.string.confirm_sign_out)) },
                text = { Text(stringResource(R.string.confirm_sign_out_desc)) },
                dismissButton = {
                    TextButton(onClick = { showSignOutDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        Firebase.auth.signOut()
                        signOutCleaner.get().run()
                    }) {
                        Text(stringResource(R.string.action_sign_out))
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.confirm_account_delete)) },
                text = { Text(stringResource(R.string.confirm_account_delete_desc)) },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(android.R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        layoutEnabled = false
                        deleteAccount(user)
                    }) {
                        Text(stringResource(R.string.action_delete_account))
                    }
                }
            )
        }
    }

    @Composable
    private fun ColumnScope.TeamDetailsCategory(userTeam: UserTeam) {
        Text(
            stringResource(R.string.team_details),
            fontSize = 19.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        var number by remember(userTeam) { mutableStateOf(userTeam.id.toString()) }
        var name by remember(userTeam) { mutableStateOf(userTeam.teamName) }

        val modifier = Modifier.fillMaxWidth()
        NumberField(
            value = number,
            onValueChange = { number = it },
            modifier = modifier,
            hint = stringResource(R.string.prompt_team_number),
            maxLength = 7
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = modifier,
            singleLine = true,
            label = { Text(stringResource(R.string.prompt_team_name)) },
        )

        Button(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp),
            onClick = {
                val nb = number.toIntOrElse(-1)
                if (nb < 0) {
                    Toast.makeText(
                        this@AccountActivity,
                        R.string.error_invalid_team_number,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@Button
                }

                if (name.isEmpty()) {
                    Toast.makeText(
                        this@AccountActivity,
                        R.string.error_invalid_team_name,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@Button
                }

                viewModel.updateUserData(UserTeam(nb, name))
            }
        ) {
            Text(stringResource(R.string.action_update))
        }
    }

    private fun deleteAccount(user: FirebaseUser) {
        Firebase.database
            .getReference(DatabasePaths.KEY_SKYSTONE)
            .child(user.uid)
            .setValue(null)

        Firebase.database
            .getReference(DatabasePaths.KEY_ULTIMATE_GOAL)
            .child(user.uid)
            .setValue(null)
            .addOnCompleteListener {
                signOutCleaner.get().run()
            }

        user.delete().addOnSuccessListener {
            finish()
        }
    }

    private fun linkWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(FirebaseConstants.WEB_CLIENT_ID)
            .requestEmail()
            .build()

        val signInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher.launch(signInClient.signInIntent)
    }

    private fun unlinkFromGoogle() {
        lifecycleScope.launch(Dispatchers.Main.immediate) {
            val task = Firebase.auth.currentUser!!.unlink(GoogleAuthProvider.PROVIDER_ID)
            task.await()

            updateLinkedProviders()
        }
    }

    private fun updateLinkedProviders() {
        linkedWithGoogle = false
        Firebase.auth.currentUser?.providerData?.let { providers ->
            linkedWithGoogle =
                providers.firstOrNull { it.providerId == GoogleAuthProvider.PROVIDER_ID } != null
        }
    }

    companion object {
        private val TAG = AccountActivity::class.simpleName!!

        private const val ARG_USER_TEAM = "user_team"

        fun startActivity(context: Context, userTeam: UserTeam?) {
            val intent = Intent(context, AccountActivity::class.java)
            intent.putExtra(ARG_USER_TEAM, userTeam)
            context.startActivity(intent)
        }
    }
}
