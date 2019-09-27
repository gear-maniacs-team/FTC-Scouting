package net.gearmaniacs.login.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import net.gearmaniacs.core.extensions.lazyFast
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.model.User
import net.gearmaniacs.login.R
import net.gearmaniacs.login.ui.fragment.LoginFragment
import net.gearmaniacs.login.ui.fragment.RegisterFragment
import net.gearmaniacs.login.utils.LoginCallback

class LoginActivity : AppCompatActivity(), LoginCallback {

    companion object {
        private const val MAIN_ACTIVITY_PACKAGE =
            "net.gearmaniacs.ftcscouting.ui.activity.MainActivity"
        private const val TAG = "LoginActivity"
        const val BUNDLE_IS_LOGIN_ACTIVE = "login_fragment_active"
    }

    private lateinit var auth: FirebaseAuth
    private val loginFragment = LoginFragment()
    private val registerFragment by lazyFast { RegisterFragment() }
    private var isLoginFragmentActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        pb_login.isEnabled = false
        pb_login.setColorSchemeResources(R.color.colorPrimary)

        if (auth.currentUser == null) {
            savedInstanceState?.let {
                isLoginFragmentActive = it.getBoolean(BUNDLE_IS_LOGIN_ACTIVE, true)
            }

            if (isLoginFragmentActive)
                setLoginFragment()
            else
                setRegisterFragment()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(BUNDLE_IS_LOGIN_ACTIVE, isLoginFragmentActive)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null)
            startMainActivity()
    }

    override fun onLogin(email: String, password: String) {
        pb_login.isRefreshing = true

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                pb_login.isRefreshing = false

                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    startMainActivity()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    longToast("Login failed")
                }
            }
    }

    override fun onRegister(user: User, email: String, password: String) {
        pb_login.isRefreshing = true

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                pb_login.isRefreshing = false

                if (task.isSuccessful) {
                    Log.d(TAG, "registerWithEmail:success")
                    registerUser(user)
                } else {
                    pb_login.isRefreshing = false
                    Log.w(TAG, "registerWithEmail:failure")
                    longToast("Registration failed.")
                }
            }
    }

    override fun switchFragment() {
        if (!isLoginFragmentActive)
            setLoginFragment()
        else
            setRegisterFragment()
    }

    private fun registerUser(user: User) {
        FirebaseDatabase.getInstance()
            .getReference(DatabasePaths.KEY_USERS)
            .child(auth.currentUser!!.uid)
            .setValue(user) { error, _ ->
                if (error == null) {
                    Log.d(TAG, "registerInDatabase:success")
                    startMainActivity()
                } else {
                    Log.w(TAG, "registerInDatabase:failure")
                    longToast("Registration failed.")
                    pb_login.isRefreshing = false
                }
            }
    }

    private fun setLoginFragment() {
        isLoginFragmentActive = true
        loginFragment.loginCallback = this

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_placeholder, loginFragment)
            commit()
        }
    }

    private fun setRegisterFragment() {
        isLoginFragmentActive = false
        registerFragment.loginCallback = this

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_placeholder, registerFragment)
            commit()
        }
    }

    private fun startMainActivity() {
        val mainActivityClass = Class.forName(MAIN_ACTIVITY_PACKAGE)
        val intent = Intent(this, mainActivityClass)

        startActivity(intent)
        finish()
    }
}
