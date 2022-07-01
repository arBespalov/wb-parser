package com.automotivecodelab.wbgoodstracker.ui.quantitychartfrag

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
import com.automotivecodelab.wbgoodstracker.databinding.FragmentQuantityChartBinding
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.ViewModelFactory
import com.automotivecodelab.wbgoodstracker.ui.chartfragment.ChartFragmentDirections
import com.automotivecodelab.wbgoodstracker.ui.init
import com.automotivecodelab.wbgoodstracker.ui.updateData
import com.google.android.material.transition.MaterialSharedAxis

class QuantityChartFragment : Fragment() {
    private val args: QuantityChartFragmentArgs by navArgs()
    private val viewModel: QuantityChartViewModel by viewModels {
        ViewModelFactory(
            requireContext().appComponent.quantityChartViewModelFactory().create(args.itemId)
        )
    }
    private var viewDataBinding: FragmentQuantityChartBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quantity_chart, container, false)
        viewDataBinding = FragmentQuantityChartBinding.bind(view).apply {
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
