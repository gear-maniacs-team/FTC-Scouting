package net.gearmaniacs.login.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_register.view.*
import net.gearmaniacs.core.extensions.getTextString
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.core.extensions.toIntOrDefault
import net.gearmaniacs.core.model.User
import net.gearmaniacs.login.R
import net.gearmaniacs.login.utils.LoginCallback

class RegisterFragment : Fragment(R.layout.fragment_register) {

    var loginCallback: LoginCallback? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.btn_email_register.setOnClickListener {
            val etId = view.et_team_number
            val etName = view.et_team_name
            val etEmail = view.et_email
            val etPassword = view.et_password
            val etConfirmPassword = view.et_confirm_password

            val id = etId.getTextString().toIntOrDefault(-1)
            val name = etName.getTextString()
            val email = etEmail.getTextString()
            val password = etPassword.getTextString()
            val confirmPassword = etConfirmPassword.getTextString()

            etId.error = null
            etName.error = null
            etEmail.error = null
            etPassword.error = null
            etConfirmPassword.error = null

            if (id < 1)
                etId.error = getString(R.string.error_invalid_team_number)

            if (name.isEmpty())
                etName.error = getString(R.string.error_invalid_team_name)

            if (!email.isValidEmail())
                etEmail.error = getString(R.string.error_invalid_email)

            if (password.length < 6)
                etPassword.error = getString(R.string.error_invalid_password)

            if (confirmPassword != password)
                etConfirmPassword.error = getString(R.string.error_incorrect_confirm_password)

            if (etId.error == null &&
                etName.error == null &&
                etEmail.error == null &&
                etPassword.error == null &&
                etConfirmPassword.error == null
            ) loginCallback?.onRegister(User(id, name), email, password)
        }

        view.btn_already_own_account.setOnClickListener {
            loginCallback?.switchFragment()
        }
    }
}
