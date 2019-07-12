package net.gearmaniacs.ftcscouting.ui.fragments.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_register.view.*
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.data.User
import net.gearmaniacs.ftcscouting.utils.extensions.getTextOrEmpty
import net.gearmaniacs.ftcscouting.utils.extensions.isValidEmail

class RegisterFragment : Fragment(R.layout.fragment_register) {

    var loginCallback: LoginInterface? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.btn_email_register.setOnClickListener {
            val etId = view.et_team_number
            val etName = view.et_team_name
            val etEmail = view.et_email
            val etPassword = view.et_password
            val etConfirmPassword = view.et_confirm_password

            val id = etId.getTextOrEmpty()
            val name = etName.getTextOrEmpty()
            val email = etEmail.getTextOrEmpty()
            val password = etPassword.getTextOrEmpty()
            val confirmPassword = etConfirmPassword.getTextOrEmpty()

            etEmail.error = null
            etPassword.error = null
            etConfirmPassword.error = null

            if (id.isEmpty())
                etId.error = getString(R.string.error_empty_team_number)
            else if (id.toInt() < 1)
                etId.error = getString(R.string.error_invalid_team_number)

            if (name.isEmpty())
                etName.error = getString(R.string.error_invalid_team_number)
            if (!email.isValidEmail())
                etEmail.error = getString(R.string.error_invalid_email)
            if (password.length < 6)
                etPassword.error = getString(R.string.error_invalid_password)
            if (confirmPassword != password)
                etConfirmPassword.error = getString(R.string.error_incorrect_confirm_password)

            if (etId.error == null && etName.error == null && etEmail.error == null && etPassword.error == null
                && etConfirmPassword.error == null
            ) {
                loginCallback?.onRegister(User(id.toInt(), name), email, password)
            }
        }

        view.tv_already_own_account.setOnClickListener {
            loginCallback?.switchFragment()
        }
    }
}
