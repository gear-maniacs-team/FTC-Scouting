package net.gearmaniacs.login.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.login.databinding.LoginBaseFragmentBinding
import net.gearmaniacs.login.interfaces.LoginBaseCallback

@AndroidEntryPoint
internal class LoginBaseFragment : Fragment() {

    private var _binding: LoginBaseFragmentBinding? = null
    private val binding get() = _binding!!

    var loginCallback: LoginBaseCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginBaseFragmentBinding.inflate(inflater, container, false)

        binding.btnSignIn.setOnClickListener {
            loginCallback?.showSignInFragment()
        }

        binding.btnSignUp.setOnClickListener {
            loginCallback?.showRegisterFragment()
        }

        binding.btnNoAccount.setOnClickListener {
            loginCallback?.useOfflineAccount()
        }

        return binding.root
    }

    override fun onDestroy() {
        loginCallback = null
        _binding = null
        super.onDestroy()
    }

    companion object {
        const val TAG = "LoginBaseFragment"
    }
}
