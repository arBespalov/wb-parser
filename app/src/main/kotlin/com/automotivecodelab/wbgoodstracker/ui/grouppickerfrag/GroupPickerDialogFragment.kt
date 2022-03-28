package com.automotivecodelab.wbgoodstracker.ui.grouppickerfrag

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.getItemsRepository
import com.automotivecodelab.wbgoodstracker.navigate
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GroupPickerDialogFragment : BottomSheetDialogFragment() {

    private val args: GroupPickerDialogFragmentArgs by navArgs()
    private val viewModel: GroupPickerDialogViewModel by viewModels {
        GroupPickerDialogViewModelFactory(getItemsRepository())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.group_picker_dialog_fragment)

        val listView = bottomSheetDialog.findViewById<ListView>(R.id.list_view)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            mutableListOf<String>()
        )
        listView?.adapter = adapter

        viewModel.groups.observe(this) { groups ->
            val groupsPlusDefaultGroupPlusNewGroup = groups
                .plus(requireContext().getString(R.string.all_items))
                .plus(requireContext().getString(R.string.new_group))
            adapter.clear()
            adapter.addAll(groupsPlusDefaultGroupPlusNewGroup)
            listView?.setOnItemClickListener { _, _, position, _ ->
                when (val chosenText = groupsPlusDefaultGroupPlusNewGroup[position]) {
                    requireContext().getString(R.string.all_items) ->
                        viewModel.setGroupToItems(args.itemsId.toList(), null)
                    requireContext().getString(R.string.new_group) -> viewModel.createNewGroup()
                    else -> viewModel.setGroupToItems(args.itemsId.toList(), chosenText)
                }
            }
        }

        bottomSheetDialog.findViewById<FrameLayout>(
            com.google.android.material.R.id.design_bottom_sheet
        )?.let { BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED }
        setupNavigation()
        return bottomSheetDialog
    }

    private fun setupNavigation() {
        // "this" instead of viewLifeCycleOwner because viewLifeCycleOwner for dialog
        // 't be initialized
        viewModel.closeDialogEvent.observe(this, EventObserver {
            dismiss()
        })

        viewModel.newGroupEvent.observe(this, EventObserver{
            val action = GroupPickerDialogFragmentDirections
                .actionGroupPickerDialogFragmentToNewGroupDialogFragment(args.itemsId)
            navigate(action)
        })
    }
}
