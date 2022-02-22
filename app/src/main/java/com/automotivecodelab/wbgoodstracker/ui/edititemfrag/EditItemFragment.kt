package com.automotivecodelab.wbgoodstracker.ui.edititemfrag

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.MyApplication
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.domain.models.Item
import com.automotivecodelab.wbgoodstracker.databinding.EditItemFragmentBinding
import com.automotivecodelab.wbgoodstracker.getItemsRepository
import com.automotivecodelab.wbgoodstracker.getUserRepository
import com.automotivecodelab.wbgoodstracker.ui.KeyboardToggle
import com.google.android.material.transition.MaterialSharedAxis

class EditItemFragment : Fragment() {

    private val viewModel: EditItemViewModel by viewModels { EditItemViewModelFactory(args.itemId, getItemsRepository()) }
    private val args: EditItemFragmentArgs by navArgs()
    private var viewDataBinding: EditItemFragmentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        val cancelButton = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_close_24, requireActivity().theme)
        viewDataBinding?.apply {
            toolbar.setupWithNavController(navController, appBarConfiguration)
            toolbar.navigationIcon =  cancelButton

            fabSave.setOnClickListener { viewModel.saveItem() }
            setupNavigation()


            //setupAutoCompleteTextView
            val groupNames = viewModel.getSavedGroupNames()
            val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_menu_list_item, groupNames)

            viewModel.item.observe(viewLifecycleOwner, Observer { item: Item? ->
                if (item != null) {
                    val string = viewModel.cachedName ?: item.local_name ?: item.name
                    name.setText(string)
                    name.post { name.setSelection(string.length) }
                    val currentGroup = viewModel.cachedGroupName ?: item.local_groupName ?: getString(R.string.all_items)
                    autoCompleteTextView.apply {
                        setText(currentGroup)
                        setAdapter(adapter)// if adapter setted before setText, setText will replace it
                    }
                }
            })
        }

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        postponeEnterTransition()
        view?.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setupNavigation() {
        viewModel.saveItemEvent.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigateUp()
        })
    }
}