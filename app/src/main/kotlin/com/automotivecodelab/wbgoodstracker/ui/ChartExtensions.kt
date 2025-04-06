package com.automotivecodelab.wbgoodstracker.ui

import android.annotation.SuppressLint
import android.graphics.Paint
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.automotivecodelab.wbgoodstracker.R
import com.automotivecodelab.wbgoodstracker.themeColor
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("RestrictedApi")
fun LineChart.init(@StringRes noDataText: Int) {
    setNoDataText(resources.getString(noDataText))
    description = null
    isKeepPositionOnRotation = true
    setDrawBorders(false)
    axisLeft.apply {
        setDrawAxisLine(false)
        textSize = 8f
        textColor = context.themeColor(com.google.android.material.R.attr.colorOnBackground)
    }
    axisRight.isEnabled = false
    xAxis.apply {
        setDrawAxisLine(false)
        // limit lines do tot works when xAxis.isEnabled = false
        isEnabled = true
        setDrawGridLines(false)
        setDrawLabels(false)
    }
    legend.isEnabled = false
    marker = CustomMarkerView(context)
    setNoDataTextColor(context.themeColor(com.google.android.material.R.attr.colorOnBackground))
    setPinchZoom(true)
}

@SuppressLint("PrivateResource", "RestrictedApi")
fun LineChart.updateData(chartData: List<Pair<Long, Int>>) {
    val entries = chartData.map { (x, y) ->
        Entry(x.toFloat(), y.toFloat())
    }
    val calendar = Calendar.getInstance().apply {
        timeInMillis = chartData.last().first
        set(Calendar.MILLISECOND, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.HOUR_OF_DAY, 0)
    }
    val labelColor = context.themeColor(com.google.android.material.R.attr.colorOnBackground)
    val weekdayColor = ContextCompat.getColor(context, R.color.red)
    xAxis.setDrawLimitLinesBehindData(true)
    val formatter = SimpleDateFormat("dd.MM", Locale("en"))
    while (calendar.timeInMillis > chartData[0].first) {
        xAxis.addLimitLine(
            LimitLine(
                calendar.timeInMillis.toFloat(),
                formatter.format(calendar.timeInMillis)
            ).apply {
                lineColor = labelColor
                lineWidth = 0.2f
                labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
                textSize = 8f
                textColor = if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                    calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    weekdayColor
                } else {
                    labelColor
                }
                // for identical labels with yAxis
                textStyle = Paint.Style.FILL
            }
        )
        calendar.add(Calendar.DATE, -1)
    }

    val dataSet = LineDataSet(entries, "exampleLabel").apply {
        val primaryColor = context.themeColor(com.google.android.material.R.attr.colorPrimary)
        this.color = primaryColor
        setCircleColor(primaryColor)
        circleHoleColor = context.themeColor(android.R.attr.colorBackground)
        setDrawValues(false)
        highLightColor = context.themeColor(com.google.android.material.R.attr.colorSurfaceVariant)
    }
    data = LineData(dataSet)
    invalidate()
}
