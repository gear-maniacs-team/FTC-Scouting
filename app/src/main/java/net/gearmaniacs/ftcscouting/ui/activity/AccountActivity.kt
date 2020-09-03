package net.gearmaniacs.ftcscouting.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updateMargins
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.alertDialog
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.extensions.textString
import net.gearmaniacs.core.extensions.toIntOrDefault
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.core.model.isNullOrEmpty
import net.gearmaniacs.core.utils.UserDataPreferences
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.databinding.AccountActivityBinding
import net.gearmaniacs.ftcscouting.viewmodel.AccountViewModel
import net.theluckycoder.database.SignOutCleaner
import javax.inject.Inject

@AndroidEntryPoint
class AccountActivity : AppCompatActivity() {

    private val viewModel by viewModels<AccountViewModel>()
    private lateinit var binding: AccountActivityBinding

    @Inject
    lateinit var userDataPreferences: UserDataPreferences

    private var linkedWithGoogle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AccountActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.bottomAppBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.bottomAppBar.doOnPreDraw { appBar ->
            binding.scrollView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                updateMargins(bottom = appBar.height)
            }
        }

        binding.btnConnectGoogle.setOnClickListener {
            if (!linkedWithGoogle)
                linkWithGoogle()
            else
                unlinkFromGoogle()
        }

        binding.btnAccountSignOut.setOnClickListener {
            alertDialog {
                setTitle(R.string.confirm_sign_out)
                setMessage(R.string.confirm_sign_out_desc)
                setPositiveButton(R.string.action_sign_out) { _, _ ->
                    Firebase.auth.signOut()
                    SignOutCleaner().run()
                }
                setNegativeButton(android.R.string.cancel, null)
                show()
            }
        }

        val originalUserData = userDataPreferences.userTeam

        if (originalUserData.isNullOrEmpty()) {
            longToast(R.string.team_details_previous_not_found)
        } else {
            binding.etTeamNumber.setText(originalUserData.id.toString())
            binding.etTeamName.setText(originalUserData.teamName)
        }

        binding.btnUpdateTeamDetails.setOnClickListener {
            val number = binding.etTeamNumber.textString.toIntOrDefault(-1)
            val teamName = binding.etTeamName.textString.trim()

            if (number < 0)
                binding.etTeamNumber.error = getString(R.string.error_invalid_team_number)

            if (teamName.isEmpty())
                binding.etTeamName.error = getString(R.string.error_invalid_team_name)

            if (binding.etTeamNumber.error != null || binding.etTeamName.error != null)
                return@setOnClickListener

            viewModel.updateUserData(UserData(number, teamName))
        }
    }

    override fun onStart() {
        super.onStart()
        binding.layoutAccount.isVisible = Firebase.isLoggedIn

        updateLinkedProviders()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun linkWithGoogle() {
        binding.btnConnectGoogle.isEnabled = false
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val signInClient = GoogleSignIn.getClient(this, gso)

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            GlobalScope.launch(Dispatchers.Main.immediate) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

                try {
                    val account = task.getResult(ApiException::class.java)!!

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    val linkTask = Firebase.auth.currentUser!!.linkWithCredential(credential)
                    linkTask.await()
                    updateLinkedProviders()

                    if (linkTask.isSuccessful)
                        toast(R.string.account_provider_google_link_success)
                    else
                        throw IllegalStateException("Could not link account with Google")
                } catch (e: Exception) {
                    Log.w(TAG, "Google Account linking failed", e)
                    longToast(R.string.account_provider_google_link_failure)
                }

                binding.btnConnectGoogle.isEnabled = true
            }
        }.launch(signInClient.signInIntent)
    }

    private fun unlinkFromGoogle() {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            binding.btnConnectGoogle.isEnabled = false

            val task = Firebase.auth.currentUser!!.unlink(GoogleAuthProvider.PROVIDER_ID)
            task.await()

            binding.btnConnectGoogle.isEnabled = true
            updateLinkedProviders()
        }
    }

    private fun updateLinkedProviders() {
        linkedWithGoogle = false
        Firebase.auth.currentUser?.providerData?.let { providers ->
            linkedWithGoogle =
                providers.firstOrNull { it.providerId == GoogleAuthProvider.PROVIDER_ID } != null
        }

        binding.btnConnectGoogle.setButtonConnected(linkedWithGoogle)
    }

    private fun MaterialButton.setButtonConnected(connected: Boolean) {
        setText(if (connected) R.string.action_disconnect else R.string.action_connect)
        //setTextColor(if ())
    }

    companion object {
        private val TAG = AccountActivity::class.simpleName!!
    }
}
