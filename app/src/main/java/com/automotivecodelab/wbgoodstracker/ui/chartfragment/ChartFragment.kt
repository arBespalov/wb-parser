package com.automotivecodelab.wbgoodstracker.ui.chartfragment

import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.automotivecodelab.wbgoodstracker.*
import com.automotivecodelab.wbgoodstracker.databinding.ChartFragmentBinding
import com.automotivecodelab.wbgoodstracker.databinding.SignInFragmentBinding
import com.automotivecodelab.wbgoodstracker.domain.models.User
import com.automotivecodelab.wbgoodstracker.ui.EventObserver
import com.automotivecodelab.wbgoodstracker.ui.detailfrag.DetailFragmentArgs
import com.automotivecodelab.wbgoodstracker.ui.signinfrag.SignInFragmentDirections
import com.automotivecodelab.wbgoodstracker.ui.signinfrag.SignInViewModel
import com.automotivecodelab.wbgoodstracker.ui.signinfrag.SignInViewModelFactory
import com.automotivecodelab.wbgoodstracker.ui.signinfrag.SignInViewState
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.material.transition.MaterialSharedAxis
import java.text.SimpleDateFormat
import java.util.*

class ChartFragment : Fragment() {
    private val args: ChartFragmentArgs by navArgs()
    private val viewModel: ChartViewModel by viewModels { ChartViewModelFactory(getItemsRepository(), args.itemId) }
    private var viewDataBinding: ChartFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chart_fragment, container, false)

        viewDataBinding = ChartFragmentBinding.bind(view).apply {
            lifecycleOwner = viewLifecycleOwner
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

        viewDataBinding?.apply {
            toolbar.setupWithNavController(navController, appBarConfiguration)
            swipeRefresh.isEnabled = false
            viewModel.dataLoading.observe(viewLifecycleOwner, Observer {
                swipeRefresh.isRefreshing = it
            })
            chart.setNoDataText(getString(R.string.please_wait))
            viewModel.chartData.observe(viewLifecycleOwner, Observer { list ->
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
            })
        }

        viewModel.start()

        setupNavigation()

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
    }

    private fun setupNavigation() {
        viewModel.networkErrorEvent.observe(viewLifecycleOwner, EventObserver {
            val action = ChartFragmentDirections.actionChartFragmentToErrorDialogFragment(it)
            navigate(action)
        })
    }
}

class MyValueFormatter: ValueFormatter() {
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return SimpleDateFormat("dd.MM HH:mm", Locale("en")).format(value)
    }
}