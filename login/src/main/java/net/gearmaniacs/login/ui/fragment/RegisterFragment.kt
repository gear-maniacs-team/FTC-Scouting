package net.gearmaniacs.login.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.getTextString
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.core.extensions.toIntOrDefault
import net.gearmaniacs.core.model.User
import net.gearmaniacs.login.R
import net.gearmaniacs.login.databinding.FragmentRegisterBinding
import net.gearmaniacs.login.interfaces.LoginCallback

@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    var loginCallback: LoginCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.btnEmailRegister.setOnClickListener {
            val etNumber = binding.etTeamNumber
            val etName = binding.etTeamName
            val etEmail = binding.etEmail
            val etPassword = binding.etPassword
            val etConfirmPassword = binding.etConfirmPassword

            val number = etNumber.getTextString().toIntOrDefault(-1)
            val name = etName.getTextString()
            val email = etEmail.getTextString()
            val password = etPassword.getTextString()
            val confirmPassword = etConfirmPassword.getTextString()

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
            ) loginCallback?.onRegister(User(number, name), email, password)
        }

        binding.btnAlreadyOwnAccount.setOnClickListener {
            loginCallback?.switchFragment()
        }

        return view
    }

    override fun onDestroy() {
        loginCallback = null
        _binding = null
        super.onDestroy()
    }
}
