package net.gearmaniacs.tournament.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.StringRes
import dagger.hilt.android.AndroidEntryPoint
import net.gearmaniacs.core.extensions.textString
import net.gearmaniacs.core.utils.RoundedBottomSheetDialogFragment
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.databinding.NewTournamentDialogBinding

@AndroidEntryPoint
class NewTournamentDialog : RoundedBottomSheetDialogFragment() {

    private var _binding: NewTournamentDialogBinding? = null
    private val binding
        get() = _binding!!

    @StringRes
    var actionButtonStringRes = R.string.action_create
    var defaultName: String? = null
    var actionButtonListener: ((name: String) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setExpanded(dialog)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewTournamentDialogBinding.inflate(layoutInflater, container, false)

        binding.btnTournamentAction.setText(actionButtonStringRes)
        binding.etTournamentName.setText(defaultName)
        binding.etTournamentName.requestFocus()

        binding.btnTournamentAction.setOnClickListener {
            val name = binding.etTournamentName.textString
            actionButtonListener?.invoke(name)
            dismiss()
        }

        return binding.root
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}