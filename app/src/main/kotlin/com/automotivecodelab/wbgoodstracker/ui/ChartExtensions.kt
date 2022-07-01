package com.automotivecodelab.wbgoodstracker.ui

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.themeColor
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*


fun LineChart.init(@StringRes noDataText: Int) {
    setNoDataText(resources.getString(noDataText))
    description = null
    isKeepPositionOnRotation = true
    setDrawBorders(false)
    axisLeft.setDrawAxisLine(false)
    axisLeft.textColor = context.themeColor(R.attr.colorOnBackground)
    axisRight.isEnabled = false
    xAxis.setDrawAxisLine(false)
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.valueFormatter = object : ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return SimpleDateFormat("dd.MM HH:mm", Locale("en")).format(value)
        }
    }
    xAxis.textColor = context.themeColor(R.attr.colorOnBackground)
    legend.isEnabled = false
    marker = CustomMarkerView(context)
    setNoDataTextColor(context.themeColor(R.attr.colorOnBackground))
    setPinchZoom(true)
}

@SuppressLint("PrivateResource")
fun LineChart.updateData(chartData: List<Pair<Long, Int>>) {
    val entries = chartData.map { (x, y) ->
        Entry(x.toFloat(), y.toFloat())
    }
    val dataSet = LineDataSet(entries, "exampleLabel").apply {
        color = context.themeColor(R.attr.colorPrimary)
        setDrawCircles(false)
        setDrawValues(false)
        highLightColor = context.themeColor(R.attr.colorSurfaceVariant)
    }
    data = LineData(dataSet)
    invalidate()
}
