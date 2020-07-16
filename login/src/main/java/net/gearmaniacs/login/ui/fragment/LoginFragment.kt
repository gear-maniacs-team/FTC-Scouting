package net.gearmaniacs.login.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.login.R
import net.gearmaniacs.login.databinding.FragmentLoginBinding
import net.gearmaniacs.login.ui.activity.ResetPasswordActivity
import net.gearmaniacs.login.interfaces.LoginCallback

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    var loginCallback: LoginCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.btnEmailSignIn.setOnClickListener {
            val etEmail = binding.etEmail
            val etPassword = binding.etPassword
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

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
}
