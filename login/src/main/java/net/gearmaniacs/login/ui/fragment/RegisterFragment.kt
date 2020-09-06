package net.gearmaniacs.login.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.core.extensions.textString
import net.gearmaniacs.core.extensions.toIntOrElse
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.login.R
import net.gearmaniacs.login.databinding.RegisterFragmentBinding
import net.gearmaniacs.login.interfaces.LoginCallback

@AndroidEntryPoint
internal class RegisterFragment : Fragment() {

    private var _binding: RegisterFragmentBinding? = null
    val binding get() = _binding!!

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
                loginCallback?.onRegister(UserTeam(number, name), email, password)
            }
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

    companion object {
        const val TAG = "RegisterFragment"
    }
}
