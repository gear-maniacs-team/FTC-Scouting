package net.gearmaniacs.tournament.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import net.gearmaniacs.core.extensions.getTextString
import net.gearmaniacs.tournament.R
import net.gearmaniacs.tournament.databinding.DialogNewTournamentBinding

class TournamentDialogFragment : RoundedBottomSheetDialogFragment() {

    private var _binding: DialogNewTournamentBinding? = null
    private val binding
        get() = _binding!!

    @StringRes
    var actionButtonStringRes = R.string.action_create
    var defaultName: String? = null
    var actionButtonListener: ((name: String) -> Unit)? = null

    override fun setupDialog(dialog: Dialog, style: Int) {
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            val bottomSheetDialog = dialog as? BottomSheetDialog
            bottomSheetDialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?.let { bottomSheet ->
                    BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNewTournamentBinding.inflate(layoutInflater, container, false)

        binding.btnTournamentAction.setText(actionButtonStringRes)
        binding.etTournamentName.setText(defaultName)
        binding.etTournamentName.requestFocus()

        binding.btnTournamentAction.setOnClickListener {
            val name = binding.etTournamentName.getTextString()
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