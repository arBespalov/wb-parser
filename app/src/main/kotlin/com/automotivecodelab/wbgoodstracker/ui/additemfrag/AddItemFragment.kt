package com.automotivecodelab.wbgoodstracker.ui.additemfrag

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat.getDrawable
import androidx.core.view.doOnPreDraw
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.automotivecodelab.wbgoodstracker.*
import com.automotivecodelab.wbgoodstracker.databinding.AddItemFragmentBinding
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.SignOutSnackbar
import com.google.android.material.transition.MaterialContainerTransform

class AddItemFragment : Fragment() {

    private val viewModel: AddItemViewModel by viewModels {
        AddItemViewModelFactory(getItemsRepository(), getUserRepository())
    }
    private var viewDataBinding: AddItemFragmentBinding? = null
    private val args: AddItemFragmentArgs by navArgs()
    private var isArgsUrlHandled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_item_fragment, container, false)
        viewDataBinding = AddItemFragmentBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        postponeEnterTransition()
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
            containerColor = requireContext().themeColor(R.attr.colorSurface)
            startContainerColor = requireContext().themeColor(R.attr.colorSecondary)
            endContainerColor = requireContext().themeColor(R.attr.colorSurface)
        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            scrimColor = Color.TRANSPARENT
            containerColor = requireContext().themeColor(R.attr.colorSurface)
            startContainerColor = requireContext().themeColor(R.attr.colorSurface)
            endContainerColor = requireContext().themeColor(R.attr.colorSecondary)
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
        val cancelButton = getDrawable(
            resources,
            R.drawable.ic_baseline_close_24,
            requireActivity().theme
        )
        viewDataBinding?.apply {
            toolbar.setupWithNavController(navController, appBarConfiguration)
            toolbar.navigationIcon = cancelButton
            swipeRefresh.isEnabled = false
            fabSave.setOnClickListener { viewModel.saveItem() }
            viewModel.invalidUrl.observe(
                viewLifecycleOwner
            ) {
                if (it) {
                    textInputLayout.error = getString(R.string.invalid_url)
                } else {
                    textInputLayout.error = null
                }
            }
            viewModel.dataLoading.observe(viewLifecycleOwner) {
                if (it) {
                    swipeRefresh.isRefreshing = true
                    fabSave.hide()
                } else {
                    swipeRefresh.isRefreshing = false
                    fabSave.show()
                }
            }
            fabSave.isEnabled = false
            URL.addTextChangedListener {
                fabSave.isEnabled = !it.isNullOrEmpty()
                viewModel.handleTextInput(it.toString())
            }
            if (!isArgsUrlHandled) {
                args.url?.let { URL.setText(it) }
                isArgsUrlHandled = true
            }

        }
        viewModel.authorizationErrorEvent.observe(viewLifecycleOwner, EventObserver {
                SignOutSnackbar().invoke(requireView()) { viewModel.signOut() }
            }
        )
        setupNavigation()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setupNavigation() {
        viewModel.saveSuccessfulEvent.observe(viewLifecycleOwner, EventObserver {
                findNavController().navigateUp()
            }
        )

        viewModel.networkErrorEvent.observe(viewLifecycleOwner, EventObserver {
                val action = AddItemFragmentDirections
                    .actionAddItemFragmentToErrorDialogFragment(it)
                navigate(action)
            }
        )
    }
}
