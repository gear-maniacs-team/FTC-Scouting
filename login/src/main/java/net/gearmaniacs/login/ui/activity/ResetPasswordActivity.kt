package net.gearmaniacs.login.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_reset_password.*
import net.gearmaniacs.core.extensions.getTextString
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.login.R

class ResetPasswordActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "ResetPasswordActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        setSupportActionBar(bottom_app_bar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        pb_reset_password.isEnabled = false

        btn_reset_password.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val email = et_email.getTextString()
            val appContext = applicationContext

            if (!email.isValidEmail()) {
                et_email.error = getString(R.string.error_invalid_email)
                return@setOnClickListener
            }

            appContext.toast(R.string.reset_password_sending)
            startLoading()

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    stopLoading()

                    if (task.isSuccessful) {
                        Log.d(TAG, "Password Reset Email sent.")
                        appContext.longToast(R.string.reset_password_email_sent)
                    } else {
                        Log.w(TAG, "Error sending Password Reset Email")
                        appContext.longToast(R.string.reset_password_email_not_sent)
                    }
                }
        }
    }

    private fun startLoading() {
        btn_reset_password.isEnabled = false
        pb_reset_password.isRefreshing = true
    }

    private fun stopLoading() {
        btn_reset_password.isEnabled = true
        pb_reset_password.isRefreshing = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
