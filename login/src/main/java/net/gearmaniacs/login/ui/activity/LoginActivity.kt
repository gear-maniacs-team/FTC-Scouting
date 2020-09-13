package net.gearmaniacs.login.ui.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.gearmaniacs.core.extensions.alertDialog
import net.gearmaniacs.core.extensions.hideKeyboard
import net.gearmaniacs.core.extensions.themeColor
import net.gearmaniacs.core.firebase.isLoggedIn
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
    lateinit var introActivityClass: IntroActivityClass

    @Inject
    lateinit var appPreferences: AppPreferences

    init {
        lifecycleScope.launchWhenResumed {
            val hasSeenIntro = appPreferences.seenIntroFlow.first()

            if (!hasSeenIntro) {
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == RESULT_OK) {
                        lifecycleScope.launch { appPreferences.setSeenIntro(true) }
                    } else {
                        finish()
                    }
                }.launch(Intent(this@LoginActivity, Class.forName(introActivityClass.value)))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

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

        loginBaseFragment.enterTransition = MaterialFadeThrough().apply {
            duration = 150L
        }

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

    override fun showSignInFragment() {
        hideKeyboard()
        activeFragmentTag = SignInFragment.TAG

        supportFragmentManager.commit {
            if (loginBaseFragment.isVisible) {
                signInFragment.sharedElementEnterTransition = getMaterialContainerTransition()
                addSharedElement(loginBaseFragment.binding.layoutContent, "primary_color_content")
            }

            show(signInFragment)
            hide(loginBaseFragment)
            hide(registerFragment)
        }
    }

    override fun showRegisterFragment() {
        hideKeyboard()
        activeFragmentTag = RegisterFragment.TAG

        supportFragmentManager.commit {
            if (loginBaseFragment.isVisible) {
                registerFragment.sharedElementEnterTransition = getMaterialContainerTransition()
                addSharedElement(loginBaseFragment.binding.btnSignUp, "register_layout_content")
            }

            hide(loginBaseFragment)
            hide(signInFragment)
            show(registerFragment)
        }
    }

    override fun showBaseFragment() {
        hideKeyboard()
        activeFragmentTag = LoginBaseFragment.TAG

        supportFragmentManager.commit {
            show(loginBaseFragment)
            hide(signInFragment)
            hide(registerFragment)
        }
    }

    override fun finishActivity() {
        startMainActivity()
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
        GlobalScope.launch {
            appPreferences.setLoggedIn(true)
        }

        val mainActivityClass = Class.forName(mainActivityClass.value)
        val intent = Intent(this, mainActivityClass)

        startActivity(intent)
        finish()
    }

    private fun getMaterialContainerTransition() = MaterialContainerTransform().apply {
        drawingViewId = R.id.fragment_placeholder
        duration = 650.toLong()
        scrimColor = Color.TRANSPARENT
        setAllContainerColors(themeColor(R.attr.colorSurface))
    }

    class MainActivityClass(val value: String)

    class IntroActivityClass(val value: String)

    private companion object {
        private const val TAG = "LoginActivity"

        private const val BUNDLE_FRAGMENT_ACTIVE = "fragment_active"
    }
}
