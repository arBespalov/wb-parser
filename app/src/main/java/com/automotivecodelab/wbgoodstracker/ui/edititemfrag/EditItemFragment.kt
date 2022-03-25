package com.automotivecodelab.wbgoodstracker.ui.edititemfrag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.doOnPreDraw
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.databinding.EditItemFragmentBinding
import com.automotivecodelab.wbgoodstracker.getItemsRepository
import com.automotivecodelab.wbgoodstracker.navigate
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.google.android.material.transition.MaterialSharedAxis

class EditItemFragment : Fragment() {

    private val viewModel: EditItemViewModel by viewModels {
        EditItemViewModelFactory(args.itemId, getItemsRepository())
    }
    private val args: EditItemFragmentArgs by navArgs()
    private var viewDataBinding: EditItemFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.edit_item_fragment, container, false)
        viewDataBinding = EditItemFragmentBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
            viewmodel = viewModel
        }
        postponeEnterTransition()
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        return view
    }

    override fun onDestroyView() {
        viewDataBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val cancelButton = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_baseline_close_24,
            requireActivity().theme
        )
        setupNavigation()
        viewDataBinding?.apply {
            toolbar.setupWithNavController(navController, appBarConfiguration)
            toolbar.navigationIcon = cancelButton

            fabSave.setOnClickListener { viewModel.saveItem() }

            // setupAutoCompleteTextView
            val defaultGroup = requireContext().getString(R.string.all_items)
            val groups = mutableListOf(defaultGroup)
            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.dropdown_menu_list_item,
                groups
            )

            autoCompleteTextView.setAdapter(adapter)

            viewModel.item.observe(viewLifecycleOwner) { item ->
                val sName = viewModel.newName ?: item.localName ?: item.name
                name.setText(sName)
                val currentGroup =
                    viewModel.newGroup ?: item.groupName ?: getString(R.string.all_items)
                autoCompleteTextView.setText(currentGroup, false)
            }

            viewModel.groups.observe(viewLifecycleOwner) { savedGroups ->
                val groupsToAdd = savedGroups.minus(groups)
                val groupsToRemove = groups.minus(savedGroups.plus(defaultGroup))
                groups.addAll(groupsToAdd)
                groups.removeAll(groupsToRemove)
            }

            name.addTextChangedListener {
                viewModel.newName = it.toString()
            }
            autoCompleteTextView.addTextChangedListener {
                when (it.toString()) {
                    defaultGroup -> viewModel.newGroup = null
                    else -> viewModel.newGroup = it.toString()
                }
            }
            newGroup.setOnClickListener {

            }
        }
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setupNavigation() {
        viewModel.closeScreenEvent.observe(viewLifecycleOwner, EventObserver {
                findNavController().navigateUp()
            }
        )

        viewModel.createNewGroupEvent.observe(viewLifecycleOwner, EventObserver {
                val action = EditItemFragmentDirections
                    .actionEditItemFragmentToNewGroupDialogFragment(arrayOf(it))
                navigate(action)
            }
        )
    }
}
