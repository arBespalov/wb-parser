package com.automotivecodelab.wbgoodstracker.ui.edititemfrag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.res.ResourcesCompat
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
        viewDataBinding?.apply {
            toolbar.setupWithNavController(navController, appBarConfiguration)
            toolbar.navigationIcon = cancelButton

            fabSave.setOnClickListener { viewModel.saveItem() }
            setupNavigation()

            // setupAutoCompleteTextView
            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.dropdown_menu_list_item,
                mutableListOf<String>()
            )

            autoCompleteTextView.apply {
                setAdapter(adapter)
            }

            viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
                adapter.clear()
                adapter.addAll(viewState.groups
                    .plus(requireContext().getString(R.string.all_items)))
                if (viewState.item != null) {
                    val sName =
                        viewModel.newName ?: viewState.item.localName ?: viewState.item.name
                    name.setText(sName)
                    name.post { name.setSelection(sName.length) }

                    val currentGroup =
                        viewModel.newGroup ?: viewState.item.groupName
                        ?: getString(R.string.all_items)
                    autoCompleteTextView.setText(currentGroup, false)
                }
            }

            name.addTextChangedListener {
                viewModel.newName = it.toString()
            }
            autoCompleteTextView.addTextChangedListener {
                if (it.toString() == requireContext().getString(R.string.all_items)) {
                    viewModel.newGroup = null
                } else {
                    viewModel.newGroup = it.toString()
                }
            }
        }

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

//        postponeEnterTransition()
//        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setupNavigation() {
        viewModel.saveItemEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                findNavController().navigateUp()
            }
        )
    }
}
