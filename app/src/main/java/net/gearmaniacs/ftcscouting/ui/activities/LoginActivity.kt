package net.gearmaniacs.ftcscouting.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.ContentView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.data.User
import net.gearmaniacs.ftcscouting.ui.fragments.login.LoginFragment
import net.gearmaniacs.ftcscouting.ui.fragments.login.LoginInterface
import net.gearmaniacs.ftcscouting.ui.fragments.login.RegisterFragment
import net.gearmaniacs.ftcscouting.utils.extensions.lazyFast
import net.gearmaniacs.ftcscouting.utils.extensions.longToast

class LoginActivity : AppCompatActivity(), LoginInterface {

    companion object {
        const val BUNDLE_IS_LOGIN_ACTIVE = "login_fragment_active"
    }

    private lateinit var auth: FirebaseAuth
    private val loginFragment by lazyFast { LoginFragment() }
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
                    Log.d("LoginActivity", "signInWithEmail:success")
                    startMainActivity()
                } else {
                    Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                    longToast("Login failed")
                }
            }
    }

    override fun onRegister(user: User, email: String, password: String) {
        pb_login.isRefreshing = true

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "registerWithEmail:success")

                    registerUser(user)
                } else {
                    pb_login.isRefreshing = false
                    Log.w("LoginActivity", "registerWithEmail:failure")
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
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(auth.currentUser!!.uid)
            .setValue(user) { error, _ ->
                if (error == null) {
                    Log.d("LoginActivity", "registerInDatabase:success")
                    startMainActivity()
                } else {
                    Log.w("LoginActivity", "registerInDatabase:failure")
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
        startActivity(Intent(this, MainActivity::class.java))
    }
}
