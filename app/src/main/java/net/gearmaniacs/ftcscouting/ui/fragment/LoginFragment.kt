package net.gearmaniacs.ftcscouting.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_login.view.*
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.utils.LoginCallback

class LoginFragment : Fragment(R.layout.fragment_login) {

    var loginCallback: LoginCallback? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.btn_email_sign_in.setOnClickListener {
            val etEmail = view.et_email
            val etPassword = view.et_password
            val email = etEmail.text?.toString().orEmpty()
            val password = etPassword.text?.toString().orEmpty()

            etEmail.error = null
            etPassword.error = null

            if (!email.isValidEmail())
                etEmail.error = getString(R.string.error_invalid_email)
            if (password.length < 6)
                etPassword.error = getString(R.string.error_invalid_password)

            if (etEmail.error == null && etPassword.error == null)
                loginCallback?.onLogin(email, password)
        }

        view.btn_no_account.setOnClickListener {
            loginCallback?.switchFragment()
        }
    }

    override fun onDestroy() {
        loginCallback = null
        super.onDestroy()
    }
}
