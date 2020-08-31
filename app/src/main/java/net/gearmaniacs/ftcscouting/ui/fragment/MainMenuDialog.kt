package net.gearmaniacs.ftcscouting.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.startActivity
import net.gearmaniacs.core.utils.AppPreferences
import net.gearmaniacs.ftcscouting.R
import net.gearmaniacs.ftcscouting.databinding.MainMenuDialogBinding
import net.gearmaniacs.ftcscouting.ui.activity.AboutActivity
import net.gearmaniacs.ftcscouting.ui.activity.TeamInfoActivity
import net.gearmaniacs.ftcscouting.viewmodel.MainViewModel
import net.gearmaniacs.login.ui.activity.LoginActivity
import net.gearmaniacs.core.utils.RoundedBottomSheetDialogFragment
import javax.inject.Inject

@AndroidEntryPoint
class MainMenuDialog : RoundedBottomSheetDialogFragment() {

    private var _binding: MainMenuDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<MainViewModel>()

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        setExpanded(dialog)
        return dialog
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
            val displayName: String =
                if (defaultDisplayName.isNullOrBlank()) appPreferences.userDataName.get() else defaultDisplayName

            binding.btnAccountSignIn.isVisible = false
            binding.tvAccountName.text = displayName
            binding.tvAccountEmail.text = currentUser.email

            binding.btnAccountSignOut.setOnClickListener {
                val activity = requireActivity()

                AlertDialog.Builder(activity)
                    .setTitle(R.string.confirm_sign_out)
                    .setTitle(R.string.confirm_sign_out_desc)
                    .setPositiveButton(R.string.action_sign_out) { _, _ ->
                        dismiss()
                        Firebase.auth.signOut()
                        viewModel.signOut(activity)

                        activity.startActivity<LoginActivity>()
                        activity.finish()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()

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
            TeamInfoActivity.startActivity(requireContext(), viewModel.getUserLiveData().value)
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
