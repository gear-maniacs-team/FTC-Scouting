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
import kotlinx.android.synthetic.main.dialog_new_tournament.*
import kotlinx.android.synthetic.main.dialog_new_tournament.view.*
import net.gearmaniacs.core.extensions.getTextString
import net.gearmaniacs.tournament.R

class TournamentDialogFragment : RoundedBottomSheetDialogFragment() {

    @StringRes
    var actionButtonStringRes = R.string.action_create
    var defaultName: String? = null
    var actionButtonListener: ((name: String) -> Unit)? = null

    override fun setupDialog(dialog: Dialog, style: Int) {
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.setOnShowListener {
            val bottomSheetDialog = dialog as BottomSheetDialog
            bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                ?.let { bottomSheet ->
                    BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
                }
        }
        super.setupDialog(dialog, style)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_new_tournament, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.btn_tournament_action.setText(actionButtonStringRes)
        view.et_tournament_name.setText(defaultName)
        view.et_tournament_name.requestFocus()

        view.btn_tournament_action.setOnClickListener {
            val name = et_tournament_name.getTextString()
            actionButtonListener?.invoke(name)
            dismiss()
        }
    }
}