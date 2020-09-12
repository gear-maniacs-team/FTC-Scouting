package net.gearmaniacs.login.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.extensions.textString
import net.gearmaniacs.core.extensions.toIntOrElse
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.FirebaseConstants
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.login.R
import net.gearmaniacs.login.databinding.RegisterFragmentBinding
import net.gearmaniacs.login.interfaces.LoginCallback

@AndroidEntryPoint
internal class RegisterFragment : Fragment() {

    private var _binding: RegisterFragmentBinding? = null
    private val binding get() = _binding!!

    var loginCallback: LoginCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RegisterFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.toolbar.setNavigationOnClickListener {
            loginCallback?.showBaseFragment()
        }

        binding.btnEmailRegister.setOnClickListener {
            val etNumber = binding.etTeamNumber
            val etName = binding.etTeamName
            val etEmail = binding.etEmail
            val etPassword = binding.etPassword
            val etConfirmPassword = binding.etConfirmPassword

            val number = etNumber.textString.toIntOrElse(-1)
            val name = etName.textString
            val email = etEmail.textString
            val password = etPassword.textString
            val confirmPassword = etConfirmPassword.textString

            etNumber.error = null
            etName.error = null
            etEmail.error = null
            etPassword.error = null
            etConfirmPassword.error = null

            if (number < 1)
                etNumber.error = getString(R.string.error_invalid_team_number)

            if (name.isEmpty())
                etName.error = getString(R.string.error_invalid_team_name)

            if (!email.isValidEmail())
                etEmail.error = getString(R.string.error_invalid_email)

            if (password.length < 6)
                etPassword.error = getString(R.string.error_invalid_password)

            if (confirmPassword != password)
                etConfirmPassword.error = getString(R.string.error_incorrect_confirm_password)

            if (etNumber.error == null &&
                etName.error == null &&
                etEmail.error == null &&
                etPassword.error == null &&
                etConfirmPassword.error == null
            ) {
                register(UserTeam(number, name), email, password)
            }
        }

        binding.btnRegisterGoogle.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(FirebaseConstants.WEB_CLIENT_ID)
                .requestEmail()
                .build()

            val signInClient = GoogleSignIn.getClient(requireActivity(), gso)

            requireActivity().registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                lifecycleScope.launch(Dispatchers.Main.immediate) {
                    ensureActive()
                    setIsLoading(false)
                    val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)

                    try {
                        val account = task.getResult(ApiException::class.java)!!

                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        val signInTask = Firebase.auth.signInWithCredential(credential)
                        signInTask.await()

                        if (signInTask.isSuccessful) {
                            Log.v(SignInFragment.TAG, "Google Sign In was successful")
                            loginCallback!!.finishActivity()
                        } else
                            throw IllegalStateException("Could not link account with Google")
                    } catch (e: Exception) {
                        Log.w(SignInFragment.TAG, "Google Account linking failed", e)
                        requireContext().longToast(R.string.account_provider_google_sign_in_failure)
                    }

                    setIsLoading(true)
                }
            }.launch(signInClient.signInIntent)
        }

        binding.btnAlreadyOwnAccount.setOnClickListener {
            loginCallback?.showSignInFragment()
        }

        return view
    }

    override fun onDestroy() {
        loginCallback = null
        _binding = null
        super.onDestroy()
    }

    private fun register(userTeam: UserTeam, email: String, password: String) {
        setIsLoading(true)

        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "registerWithEmail:success")
                    registerUserInDatabase(userTeam)
                } else {
                    Log.w(TAG, "registerWithEmail:failure")
                    requireContext().longToast("Registration failed.")
                    setIsLoading(false)
                }
            }
    }

    private fun registerUserInDatabase(userTeam: UserTeam) {
        Firebase.database
            .getReference(DatabasePaths.KEY_USERS)
            .child(Firebase.auth.currentUser!!.uid)
            .setValue(userTeam) { error, _ ->
                if (error == null) {
                    Log.d(TAG, "registerInDatabase:success")
                    loginCallback!!.finishActivity()
                } else {
                    Log.e(TAG, "registerInDatabase:failure")
                }
                setIsLoading(false)
            }
    }

    private fun setIsLoading(enabled: Boolean) {
        with(binding) {
            linearLayout.isEnabled = enabled
            pbRegister.isRefreshing = !enabled
        }
    }

    companion object {
        const val TAG = "RegisterFragment"
    }
}
