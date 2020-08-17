package net.gearmaniacs.login.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.core.extensions.textString
import net.gearmaniacs.login.R
import net.gearmaniacs.login.databinding.LoginFragmentBinding
import net.gearmaniacs.login.interfaces.LoginCallback
import net.gearmaniacs.login.ui.activity.ResetPasswordActivity

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.login_fragment) {

    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    var loginCallback: LoginCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

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
                loginCallback?.onLogin(email, password)
        }

        binding.btnNoAccount.setOnClickListener {
            loginCallback?.switchFragment()
        }

        binding.btnForgotPassword.setOnClickListener {
            if (loginCallback?.isWorking() == false)
                context?.startActivity<ResetPasswordActivity>()
        }

        return view
    }

    override fun onDestroy() {
        loginCallback = null
        _binding = null
        super.onDestroy()
    }

    companion object {
        const val TAG = "LoginFragment"
    }
}
