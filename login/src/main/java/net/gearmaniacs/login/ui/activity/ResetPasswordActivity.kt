package net.gearmaniacs.login.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import net.gearmaniacs.core.extensions.getTextString
import net.gearmaniacs.core.extensions.isValidEmail
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.extensions.toast
import net.gearmaniacs.login.R
import net.gearmaniacs.login.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.bottomAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.pbResetPassword.isEnabled = false

        binding.btnResetPassword.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val email = binding.etEmail.getTextString()
            val appContext = applicationContext

            if (!email.isValidEmail()) {
                binding.etEmail.error = getString(R.string.error_invalid_email)
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
        binding.btnResetPassword.isEnabled = false
        binding.pbResetPassword.isRefreshing = true
    }

    private fun stopLoading() {
        binding.btnResetPassword.isEnabled = true
        binding.pbResetPassword.isRefreshing = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private companion object {
        private const val TAG = "ResetPasswordActivity"
    }
}
