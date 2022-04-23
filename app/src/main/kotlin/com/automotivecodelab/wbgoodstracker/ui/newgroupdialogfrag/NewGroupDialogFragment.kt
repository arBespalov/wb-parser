package com.automotivecodelab.wbgoodstracker.ui.newgroupdialogfrag

import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.appComponent
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.KeyboardToggle
import com.automotivecodelab.wbgoodstracker.ui.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NewGroupDialogFragment : BottomSheetDialogFragment() {

    private val viewModel: NewGroupDialogViewModel by viewModels {
        ViewModelFactory(requireContext().appComponent.newGroupDialogViewModel())
    }
    private val args: NewGroupDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(R.layout.new_group_dialog_fragment)
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        val editText = bottomSheetDialog.findViewById<EditText>(R.id.group_name)!!
        if (args.renameGroup) {
            viewModel.getCurrentGroup()
            viewModel.currentGroup.observe(this) { group: String? ->
                if (group != null) {
                    editText.setText(group)
                    editText.setSelection(group.length)
                }
            }
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    val newGroupName = editText.text.toString()
                    if (newGroupName.isNotEmpty()) {
                        if (args.renameGroup)
                            viewModel.renameGroup(newGroupName)
                        else if (args.itemIds != null)
                            viewModel.addGroup(args.itemIds!!.toList(), newGroupName)
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
        viewModel.closeDialogEvent.observe(
            this,
            EventObserver {
                findNavController().navigateUp()
            }
        )
    }
}
