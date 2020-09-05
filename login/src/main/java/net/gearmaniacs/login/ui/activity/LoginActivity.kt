package net.gearmaniacs.login.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.gearmaniacs.core.extensions.alertDialog
import net.gearmaniacs.core.extensions.hideKeyboard
import net.gearmaniacs.core.extensions.longToast
import net.gearmaniacs.core.firebase.DatabasePaths
import net.gearmaniacs.core.firebase.isLoggedIn
import net.gearmaniacs.core.model.UserTeam
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.login.R
import net.gearmaniacs.login.databinding.LoginActivityBinding
import net.gearmaniacs.login.interfaces.LoginCallback
import net.gearmaniacs.login.ui.fragment.LoginBaseFragment
import net.gearmaniacs.login.ui.fragment.RegisterFragment
import net.gearmaniacs.login.ui.fragment.SignInFragment
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), LoginCallback {

    private lateinit var binding: LoginActivityBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var loginBaseFragment: LoginBaseFragment
    private lateinit var signInFragment: SignInFragment
    private lateinit var registerFragment: RegisterFragment

    private var activeFragmentTag = LoginBaseFragment.TAG

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

        initFragments(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(BUNDLE_FRAGMENT_ACTIVE, activeFragmentTag)
        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()

        if (Firebase.isLoggedIn)
            startMainActivity()
    }

    override fun onBackPressed() {
        if (activeFragmentTag == SignInFragment.TAG || activeFragmentTag == RegisterFragment.TAG)
            showBaseFragment()
    }

    override fun onSignIn(email: String, password: String) {
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

    override fun onRegister(userTeam: UserTeam, email: String, password: String) {
        binding.pbLogin.isRefreshing = true

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.pbLogin.isRefreshing = false

                if (task.isSuccessful) {
                    Log.d(TAG, "registerWithEmail:success")
                    registerUser(userTeam)
                } else {
                    Log.w(TAG, "registerWithEmail:failure")
                    longToast("Registration failed.")
                }
            }
    }

    override fun isWorking(): Boolean = binding.pbLogin.isRefreshing

    private fun initFragments(savedInstanceState: Bundle?) {
        loginBaseFragment =
            supportFragmentManager.findFragmentByTag(LoginBaseFragment.TAG) as? LoginBaseFragment?
                ?: LoginBaseFragment()

        signInFragment =
            supportFragmentManager.findFragmentByTag(SignInFragment.TAG) as? SignInFragment?
                ?: SignInFragment()

        registerFragment =
            supportFragmentManager.findFragmentByTag(RegisterFragment.TAG) as? RegisterFragment?
                ?: RegisterFragment()

        loginBaseFragment.loginCallback = this
        signInFragment.loginCallback = this
        registerFragment.loginCallback = this

        val fade = MaterialFadeThrough().apply {
            duration = FADE_DURATION
        }

        signInFragment.enterTransition = fade
        registerFragment.enterTransition = fade

        activeFragmentTag =
            savedInstanceState?.getString(BUNDLE_FRAGMENT_ACTIVE) ?: LoginBaseFragment.TAG

        supportFragmentManager.commit {
            if (savedInstanceState == null) {
                add(R.id.fragment_placeholder, loginBaseFragment, LoginBaseFragment.TAG)
                add(R.id.fragment_placeholder, signInFragment, SignInFragment.TAG)
                add(R.id.fragment_placeholder, registerFragment, RegisterFragment.TAG)
            }

            when (activeFragmentTag) {
                SignInFragment.TAG -> {
                    hide(loginBaseFragment)
                    hide(registerFragment)
                }
                RegisterFragment.TAG -> {
                    hide(loginBaseFragment)
                    hide(signInFragment)
                }
                else -> {
                    hide(signInFragment)
                    hide(registerFragment)
                }
            }
        }
    }

    private fun registerUser(userTeam: UserTeam) {
        Firebase.database
            .getReference(DatabasePaths.KEY_USERS)
            .child(auth.currentUser!!.uid)
            .setValue(userTeam) { error, _ ->
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

    override fun showSignInFragment() {
        if (binding.pbLogin.isRefreshing) return
        hideKeyboard()
        activeFragmentTag = SignInFragment.TAG

        supportFragmentManager.commit {
            hide(loginBaseFragment)
            show(signInFragment)
            hide(registerFragment)
        }
    }

    override fun showRegisterFragment() {
        if (binding.pbLogin.isRefreshing) return
        hideKeyboard()
        activeFragmentTag = RegisterFragment.TAG

        supportFragmentManager.commit {
            hide(loginBaseFragment)
            hide(signInFragment)
            show(registerFragment)
        }
    }

    override fun showBaseFragment() {
        if (binding.pbLogin.isRefreshing) return
        hideKeyboard()
        activeFragmentTag = LoginBaseFragment.TAG

        supportFragmentManager.commit {
            show(loginBaseFragment)
            hide(signInFragment)
            hide(registerFragment)
        }
    }

    override fun useOfflineAccount() {
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

    private fun startMainActivity() {
        lifecycleScope.launch {
            appPreferences.setLoggedIn(true)
        }

        val mainActivityClass = Class.forName(mainActivityClass.value)
        val intent = Intent(this, mainActivityClass)

        startActivity(intent)
        finish()
    }

    private companion object {
        private const val FADE_DURATION = 150L

        private const val TAG = "LoginActivity"

        private const val BUNDLE_FRAGMENT_ACTIVE = "fragment_active"
    }

    class MainActivityClass(val value: String)
}
