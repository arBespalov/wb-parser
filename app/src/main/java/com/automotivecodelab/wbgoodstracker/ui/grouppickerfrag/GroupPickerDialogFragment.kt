package com.automotivecodelab.wbgoodstracker.ui.grouppickerfrag

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.MyApplication
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.getItemsRepository
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GroupPickerDialogFragment : BottomSheetDialogFragment() {

    private val args: GroupPickerDialogFragmentArgs by navArgs()
    private val viewModel: GroupPickerDialogViewModel by viewModels { GroupPickerDialogViewModelFactory(getItemsRepository()) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.group_picker_dialog_fragment)

        val listView = bottomSheetDialog.findViewById<ListView>(R.id.list_view)
        val groupNames = viewModel.getGroupNames()
        ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, groupNames).also {
            listView?.adapter = it
        }

        listView?.setOnItemClickListener { parent, view, position, id ->
            viewModel.setGroupToItems(args.itemsId.toList(), groupNames[position])
        }

        bottomSheetDialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.let { BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED }
        setupNavigation()
        return bottomSheetDialog
    }


    private fun setupNavigation() {
        viewModel.taskCompletedEvent.observe(this, EventObserver {//"this" instead of viewLifeCycleOwner because viewLifeCycleOwner for dialog won't be initialized
            dismiss()
        })
    }
}