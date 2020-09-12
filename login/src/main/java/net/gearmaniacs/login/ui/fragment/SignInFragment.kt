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
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.core.extensions.textString
import net.gearmaniacs.core.firebase.FirebaseConstants
import net.gearmaniacs.login.R
import net.gearmaniacs.login.databinding.SignInFragmentBinding
import net.gearmaniacs.login.interfaces.LoginCallback
import net.gearmaniacs.login.ui.activity.ResetPasswordActivity

@AndroidEntryPoint
internal class SignInFragment : Fragment() {

    private var _binding: SignInFragmentBinding? = null
    private val binding get() = _binding!!

    var loginCallback: LoginCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SignInFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        with(binding.pbSignIn) {
            isEnabled = false
            setColorSchemeResources(R.color.colorPrimary)
        }

        binding.toolbar.setNavigationOnClickListener {
            loginCallback?.showBaseFragment()
        }

        binding.btnEmailSignIn.setOnClickListener {
            val etEmail = binding.etEmail
            val etPassword = binding.etPassword
            val email = etEmail.textString
            val password = etPassword.textString

            etEmail.error = null
            etPassword.error = null

            if (!email.isValidEmail())
                etEmail.error = getString(R.string.error_invalid_email)
            if (password.length < 6)
                etPassword.error = getString(R.string.error_invalid_password)

            if (etEmail.error == null && etPassword.error == null)
                signIn(email, password)
        }

        binding.btnLoginGoogle.setOnClickListener {
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
                            Log.v(TAG, "Google Sign In was successful")
                            loginCallback!!.finishActivity()
                        } else
                            throw IllegalStateException("Could not link account with Google")
                    } catch (e: Exception) {
                        Log.w(TAG, "Google Account linking failed", e)
                        requireContext().longToast(R.string.account_provider_google_sign_in_failure)
                    }

                    setIsLoading(true)
                }
            }.launch(signInClient.signInIntent)
        }

        binding.btnNoAccount.setOnClickListener {
            loginCallback?.showRegisterFragment()
        }

        binding.btnForgotPassword.setOnClickListener {
            context?.startActivity<ResetPasswordActivity>()
        }

        return view
    }

    private fun setIsLoading(enabled: Boolean) {
        with(binding) {
            constraintLayout.isEnabled = enabled
            pbSignIn.isRefreshing = !enabled
        }
    }

    private fun signIn(email: String, password: String) {
        val context = requireContext().applicationContext
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    loginCallback!!.finishActivity()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    context.longToast("Login failed")
                }
            }
    }

    override fun onDestroy() {
        loginCallback = null
        _binding = null
        super.onDestroy()
    }

    companion object {
        const val TAG = "SignInFragment"
    }
}
