package net.gearmaniacs.ftcscouting.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.alertDialog
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.core.utils.RoundedBottomSheetDialogFragment
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.databinding.MainMenuDialogBinding
import net.gearmaniacs.ftcscouting.ui.activity.AboutActivity
import net.gearmaniacs.login.ui.activity.AccountActivity
import net.gearmaniacs.ftcscouting.viewmodel.MainViewModel
import net.gearmaniacs.login.ui.activity.LoginActivity
import net.theluckycoder.database.SignOutCleaner
import javax.inject.Inject

@AndroidEntryPoint
class MainMenuDialog : RoundedBottomSheetDialogFragment() {

    private var _binding: MainMenuDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<MainViewModel>()

    @Inject
    lateinit var signOutCleaner: Lazy<SignOutCleaner>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            setExpanded(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainMenuDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = binding
        val currentUser = Firebase.auth.currentUser

        if (currentUser == null) {
            binding.layoutAccount.isVisible = false
        } else {
            val defaultDisplayName = currentUser.displayName
            val teamName = viewModel.getUserTeamLiveData().value?.teamName.orEmpty()
            val displayName: String =
                if (defaultDisplayName.isNullOrBlank()) teamName else defaultDisplayName

            binding.btnAccountSignIn.isVisible = false
            binding.tvAccountName.text = displayName
            binding.tvAccountEmail.text = currentUser.email

            binding.btnAccountSignOut.setOnClickListener {
                val activity = requireActivity()

                activity.alertDialog {
                    setTitle(R.string.confirm_sign_out)
                    setMessage(R.string.confirm_sign_out_desc)
                    setPositiveButton(R.string.action_sign_out) { _, _ ->
                        this@MainMenuDialog.dismiss()
                        Firebase.auth.signOut()
                        signOutCleaner.get().run()

                        activity.startActivity<LoginActivity>()
                        activity.finish()
                    }
                    setNegativeButton(android.R.string.cancel, null)
                    show()
                }
            }
        }

        binding.btnAccountSignIn.setOnClickListener {
            dismiss()
            with(requireActivity()) {
                startActivity<LoginActivity>()
                finish()
            }
        }

        binding.btnTeamInfo.setOnClickListener {
            dismiss()
            AccountActivity.startActivity(requireContext(), viewModel.getUserTeamLiveData().value)
        }

        binding.btnAbout.setOnClickListener {
            dismiss()
            requireContext().startActivity<AboutActivity>()
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    companion object {
        fun newInstance() = MainMenuDialog()
    }
}
