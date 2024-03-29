package com.automotivecodelab.wbgoodstracker.ui.chartfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.automotivecodelab.wbgoodstracker.*
import com.automotivecodelab.wbgoodstracker.databinding.ChartFragmentBinding
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.ViewModelFactory
import com.automotivecodelab.wbgoodstracker.ui.init
import com.automotivecodelab.wbgoodstracker.ui.updateData
import com.google.android.material.transition.MaterialSharedAxis

class ChartFragment : Fragment() {
    private val args: ChartFragmentArgs by navArgs()
    private val viewModel: ChartViewModel by viewModels {
        ViewModelFactory(
            requireContext().appComponent.chartViewModelFactory().create(args.itemId)
        )
    }
    private var viewDataBinding: ChartFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chart_fragment, container, false)
        viewDataBinding = ChartFragmentBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
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
        viewDataBinding?.apply {
            toolbar.setupWithNavController(navController, appBarConfiguration)
            swipeRefresh.isEnabled = false
            viewModel.dataLoading.observe(viewLifecycleOwner) {
                swipeRefresh.isRefreshing = it
            }

            chart.init(R.string.please_wait)
            viewModel.chartData.observe(viewLifecycleOwner) { list ->
                chart.updateData(list)
            }
        }
        setupNavigation()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setupNavigation() {
        viewModel.networkErrorEvent.observe(
            viewLifecycleOwner,
            EventObserver {
                val action = ChartFragmentDirections.actionChartFragmentToErrorDialogFragment(it)
                navigate(action)
            }
        )
    }
}
