package com.automotivecodelab.wbgoodstracker.ui.grouppickerfrag

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.appComponent
import com.automotivecodelab.wbgoodstracker.navigate
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GroupPickerDialogFragment : BottomSheetDialogFragment() {

    private val args: GroupPickerDialogFragmentArgs by navArgs()
    private val viewModel: GroupPickerDialogViewModel by viewModels {
        ViewModelFactory(requireContext().appComponent.groupPickerDialogViewModel())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.group_picker_item,
            R.id.text1,
            mutableListOf<String>()
        )

        val bottomSheetDialog = BottomSheetDialog(requireContext()).apply {
            setContentView(R.layout.group_picker_dialog_fragment)
            findViewById<ListView>(R.id.list_view)?.apply {
                this.adapter = adapter
                setOnItemClickListener { _, view, _, _ ->
                    when (val chosenText = view.findViewById<TextView>(R.id.text1).text) {
                        requireContext().getString(R.string.all_items) ->
                            viewModel.setGroupToItems(args.itemsId.toList(), null)
                        requireContext().getString(R.string.new_group) -> viewModel.createNewGroup()
                        else -> viewModel.setGroupToItems(
                            args.itemsId.toList(),
                            chosenText.toString()
                        )
                    }
                }
            }
        }

        viewModel.groups.observe(this) { (_, groups) ->
            val groupsPlusDefaultGroupPlusNewGroup = groups
                .map { (name, _) -> name }
                .plus(requireContext().getString(R.string.all_items))
                .plus(requireContext().getString(R.string.new_group))
            adapter.clear()
            adapter.addAll(groupsPlusDefaultGroupPlusNewGroup)
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
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
                .actionGroupPickerDialogFragmentToNewGroupDialogFragment(args.itemsId, false)
            navigate(action)
        })
    }
}
