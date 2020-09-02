package net.gearmaniacs.login.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.alertDialog
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.UserData
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.login.R
import net.gearmaniacs.login.databinding.LoginActivityBinding
import net.gearmaniacs.login.interfaces.LoginCallback
import net.gearmaniacs.login.ui.fragment.LoginFragment
import net.gearmaniacs.login.ui.fragment.RegisterFragment
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), LoginCallback {

    private lateinit var binding: LoginActivityBinding
    private lateinit var auth: FirebaseAuth
    private var isLoginFragmentActive = true

    private lateinit var loginFragment: LoginFragment
    private lateinit var registerFragment: RegisterFragment

    @Inject
    lateinit var mainActivityClass: MainActivityClass

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        with(binding.pbLogin) {
            isEnabled = false
            setColorSchemeResources(R.color.colorPrimary)
        }

        if (Firebase.isLoggedIn) {
            // If the user is logged in the MainActivity will be launched in onStart
            return
        }

        binding.btnUseOffline.setOnClickListener {
            alertDialog {
                setTitle("Use offline Account?")
                setMessage("")
                // TODO Add a warning
                setPositiveButton("Agree") { _, _ ->
                    startMainActivity()
                }
                setNegativeButton(android.R.string.cancel, null)
                show()
            }
        }

        initFragments(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(BUNDLE_IS_LOGIN_ACTIVE, isLoginFragmentActive)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()

        if (Firebase.isLoggedIn)
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

    override fun isWorking(): Boolean = binding.pbLogin.isRefreshing

    private fun initFragments(savedInstanceState: Bundle?) {
        loginFragment =
            supportFragmentManager.findFragmentByTag(LoginFragment.TAG) as? LoginFragment?
                ?: LoginFragment()

        registerFragment =
            supportFragmentManager.findFragmentByTag(RegisterFragment.TAG) as? RegisterFragment?
                ?: RegisterFragment()

        val fade = MaterialFadeThrough().apply {
            duration = FADE_DURATION
        }

        loginFragment.enterTransition = fade
        registerFragment.enterTransition = fade

        isLoginFragmentActive =
            savedInstanceState?.getBoolean(BUNDLE_IS_LOGIN_ACTIVE, true) ?: true

        supportFragmentManager.commit {
            if (savedInstanceState == null) {
                add(R.id.fragment_placeholder, loginFragment, LoginFragment.TAG)
                add(R.id.fragment_placeholder, registerFragment, RegisterFragment.TAG)
            }

            if (isLoginFragmentActive) {
                loginFragment.loginCallback = this@LoginActivity
                hide(registerFragment)
            } else {
                registerFragment.loginCallback = this@LoginActivity
                hide(loginFragment)
            }
        }
    }

    override fun switchFragment() {
        // Don't allow fragment switching while processing a request
        if (binding.pbLogin.isRefreshing) return

        if (!isLoginFragmentActive)
            showLoginFragment()
        else
            showRegisterFragment()
    }

    private fun registerUser(userData: UserData) {
        Firebase.database
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

    private fun showLoginFragment() {
        isLoginFragmentActive = true
        loginFragment.loginCallback = this
        registerFragment.loginCallback = null

        supportFragmentManager.commit {
            show(loginFragment)
            hide(registerFragment)
        }
    }

    private fun showRegisterFragment() {
        isLoginFragmentActive = false
        loginFragment.loginCallback = null
        registerFragment.loginCallback = this

        supportFragmentManager.commit {
            hide(loginFragment)
            show(registerFragment)
        }
    }

    private fun startMainActivity() {
        appPreferences.firstStartUp.set(false)

        val mainActivityClass = Class.forName(mainActivityClass.value)
        val intent = Intent(this, mainActivityClass)

        startActivity(intent)
        finish()
    }

    private companion object {
        private const val FADE_DURATION = 150L

        private const val TAG = "LoginActivity"

        private const val BUNDLE_IS_LOGIN_ACTIVE = "login_fragment_active"
    }

    class MainActivityClass(val value: String)
}
