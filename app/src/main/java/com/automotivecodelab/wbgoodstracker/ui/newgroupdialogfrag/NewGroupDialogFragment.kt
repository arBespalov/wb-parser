package com.automotivecodelab.wbgoodstracker.ui.newgroupdialogfrag

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.getItemsRepository
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.KeyboardToggle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NewGroupDialogFragment : BottomSheetDialogFragment() {

    private val viewModel: NewGroupDialogViewModel by viewModels {
        NewGroupDialogViewModelFactory(getItemsRepository())
    }
    private val args: NewGroupDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.fragment_new_group_dialog)

        bottomSheetDialog.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        bottomSheetDialog.setOnShowListener {
            val bottomSheet = (bottomSheetDialog as? BottomSheetDialog)
                ?.findViewById<View>(R.id.design_bottom_sheet) as? FrameLayout
            bottomSheet?.let {
                BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        val editText = bottomSheetDialog.findViewById<EditText>(R.id.group_name)!!

        editText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    val newGroupName = editText.text.toString()
                    if (newGroupName.isNotEmpty()) {
                        viewModel.addGroup(args.itemIds.toList(), newGroupName)
                    }
                    true
                }
                else -> false
            }
        }

        (requireActivity() as KeyboardToggle).showKeyboard(editText)

        setupNavigation()

        return bottomSheetDialog
    }

    private fun setupNavigation() {
        // "this" instead of viewLifeCycleOwner because viewLifeCycleOwner for dialog
        // won't be initialized
        viewModel.closeDialogEvent.observe(this, EventObserver {
            findNavController().navigateUp()
        })
    }
}
