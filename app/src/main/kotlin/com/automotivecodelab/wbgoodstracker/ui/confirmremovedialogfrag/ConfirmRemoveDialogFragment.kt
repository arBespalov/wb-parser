package com.automotivecodelab.wbgoodstracker.ui.confirmremovedialogfrag

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.appComponent
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
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.confirm_remove_bottom_sheet)

        bottomSheetDialog.findViewById<Button>(R.id.cancel)?.setOnClickListener { dismiss() }
        bottomSheetDialog.findViewById<Button>(R.id.ok)?.setOnClickListener {
            viewModel.deleteItems(args.itemsIdToDelete)
        }

        bottomSheetDialog.findViewById<FrameLayout>(
            com.google.android.material.R.id.design_bottom_sheet
        )?.let {
            BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
        }

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
