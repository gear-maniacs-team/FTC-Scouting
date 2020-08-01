package net.gearmaniacs.login.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.login.R
import net.gearmaniacs.login.databinding.ActivityLoginBinding
import net.gearmaniacs.login.ui.fragment.LoginFragment
import net.gearmaniacs.login.ui.fragment.RegisterFragment
import net.gearmaniacs.login.interfaces.LoginCallback
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), LoginCallback {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var isLoginFragmentActive = true

    @Inject
    lateinit var mainActivityClass: MainActivityClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        with(binding.pbLogin) {
            isEnabled = false
            setColorSchemeResources(R.color.colorPrimary)
        }

        if (auth.currentUser != null) {
            // If the user is logged in the MainActivity will be launched in onStart
            return
        }

        if (savedInstanceState == null) {
            setLoginFragment()
        } else {
            isLoginFragmentActive = savedInstanceState.getBoolean(BUNDLE_IS_LOGIN_ACTIVE, true)

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
        binding.pbLogin.isRefreshing = true

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.pbLogin.isRefreshing = false

                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    startMainActivity()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    longToast("Login failed")
                }
            }
    }

    override fun onRegister(userData: UserData, email: String, password: String) {
        binding.pbLogin.isRefreshing = true

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.pbLogin.isRefreshing = false

                if (task.isSuccessful) {
                    Log.d(TAG, "registerWithEmail:success")
                    registerUser(userData)
                } else {
                    Log.w(TAG, "registerWithEmail:failure")
                    longToast("Registration failed.")
                }
            }
    }

    override fun switchFragment() {
        // Don't allow fragment switching while processing a request
        if (binding.pbLogin.isRefreshing) return

        if (!isLoginFragmentActive)
            setLoginFragment()
        else
            setRegisterFragment()
    }

    override fun isWorking(): Boolean = binding.pbLogin.isRefreshing

    private fun registerUser(userData: UserData) {
        FirebaseDatabase.getInstance()
            .getReference(DatabasePaths.KEY_USERS)
            .child(auth.currentUser!!.uid)
            .setValue(userData) { error, _ ->
                binding.pbLogin.isRefreshing = false

                if (error == null) {
                    Log.d(TAG, "registerInDatabase:success")
                    startMainActivity()
                } else {
                    Log.w(TAG, "registerInDatabase:failure")
                    longToast(R.string.error_register_failed)
                }
            }
    }

    private fun setLoginFragment() {
        val fragment = supportFragmentManager
            .findFragmentByTag(TAG_LOGIN_FRAGMENT) as? LoginFragment ?: LoginFragment()

        isLoginFragmentActive = true
        fragment.loginCallback = this

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_placeholder, fragment, TAG_LOGIN_FRAGMENT)
            commit()
        }
    }

    private fun setRegisterFragment() {
        val fragment = supportFragmentManager
            .findFragmentByTag(TAG_FRAGMENT_REGISTER) as? RegisterFragment ?: RegisterFragment()

        isLoginFragmentActive = false
        fragment.loginCallback = this

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_placeholder, fragment, TAG_FRAGMENT_REGISTER)
            commit()
        }
    }

    private fun startMainActivity() {
        val mainActivityClass = Class.forName(mainActivityClass.value)
        val intent = Intent(this, mainActivityClass)

        startActivity(intent)
        finish()
    }

    private companion object {
        private const val TAG = "LoginActivity"
        private const val TAG_LOGIN_FRAGMENT = "LOGIN_FRAGMENT"
        private const val TAG_FRAGMENT_REGISTER = "REGISTER_FRAGMENT"

        private const val BUNDLE_IS_LOGIN_ACTIVE = "login_fragment_active"
    }

    class MainActivityClass(val value: String)
}
