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
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.transition.MaterialSharedAxis
import java.text.SimpleDateFormat
import java.util.*

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
            chart.setNoDataText(getString(R.string.please_wait))
            viewModel.chartData.observe(viewLifecycleOwner) { list ->
                val entries = list.map {
                    Entry(it.first.toFloat(), it.second.toFloat())
                }
                val dataSet = LineDataSet(entries, "exampleLabel")
                dataSet.color = requireContext().themeColor(R.attr.colorPrimary)
                dataSet.setDrawCircles(false)
                dataSet.setDrawValues(false)
                val lineData = LineData(dataSet)
                chart.apply {
                    data = lineData
                    description = null
                    isKeepPositionOnRotation = true
                    setDrawBorders(false)
                    axisLeft.setDrawAxisLine(false)
                    axisLeft.textColor = requireContext().themeColor(R.attr.colorOnBackground)
                    axisRight.isEnabled = false
                    xAxis.setDrawAxisLine(false)
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.valueFormatter = MyValueFormatter()
                    xAxis.textColor = requireContext().themeColor(R.attr.colorOnBackground)
                    legend.isEnabled = false
                    invalidate()
                }
            }
        }
        setupNavigation()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setupNavigation() {
        viewModel.networkErrorEvent.observe(viewLifecycleOwner, EventObserver {
                val action = ChartFragmentDirections.actionChartFragmentToErrorDialogFragment(it)
                navigate(action)
            }
        )
    }
}

class MyValueFormatter : ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return SimpleDateFormat("dd.MM HH:mm", Locale("en")).format(value)
    }
}
