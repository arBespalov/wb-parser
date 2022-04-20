package com.automotivecodelab.wbgoodstracker.ui.confirmremovedialogfrag

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.appComponent
import com.automotivecodelab.wbgoodstracker.syncErrorSnackbar
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfirmRemoveDialogFragment : BottomSheetDialogFragment() {

    private val args: ConfirmRemoveDialogFragmentArgs by navArgs()
    private val viewModel: ConfirmRemoveDialogViewModel by viewModels {
        ViewModelFactory(requireContext().appComponent.confirmRemoveDialogViewModel())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(R.layout.confirm_remove_bottom_sheet)
            findViewById<Button>(R.id.cancel)?.setOnClickListener { dismiss() }
            findViewById<Button>(R.id.ok)?.setOnClickListener {
                viewModel.deleteItems(args.itemsIdToDelete.toList())
            }
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        viewModel.authorizationErrorEvent.observe(this, EventObserver {
            parentFragment?.view?.syncErrorSnackbar()
        })
        setupNavigation()
        return bottomSheetDialog
    }

    private fun setupNavigation() {
        // "this" instead of viewLifeCycleOwner because viewLifeCycleOwner for dialog won't be initialized
        viewModel.taskCompletedEvent.observe(this, EventObserver {
                dismiss()
            }
        )
    }
}
