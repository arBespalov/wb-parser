package com.automotivecodelab.wbgoodstracker.ui

import android.annotation.SuppressLint
import android.content.Context
import com.github.mikephil.charting.utils.MPPointF
import com.automotivecodelab.wbgoodstracker.R
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.automotivecodelab.wbgoodstracker.dp
import com.automotivecodelab.wbgoodstracker.themeColor
import com.github.mikephil.charting.components.IMarker
import com.google.android.material.resources.TextAppearance
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs), IMarker {

    private var count = ""
    private var date = ""
    private val textSize = 12.dp
    private val textPadding = 6.dp
    private val cornerRadius = 10.dp

    @SuppressLint("PrivateResource")
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = context.themeColor(com.google.android.material.R.attr.colorSurfaceVariant)
    }

    @SuppressLint("RestrictedApi")
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        val textAppearance = TextAppearance(context, R.style.Body)
        textSize = this@CustomMarkerView.textSize
        color = textAppearance.textColor?.defaultColor ?: error("Undefined text color")
    }

    override fun getOffset(): MPPointF {
        return MPPointF(0f, 0f)
    }

    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        return offset
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        count = e?.y?.toInt().toString()
        date = SimpleDateFormat("dd.MM HH:mm", Locale("en")).format(e?.x)
        invalidate()
    }

    override fun draw(canvas: Canvas?, posX: Float, posY: Float) {
        val countWidth = textPaint.measureText(count)
        val dateWidth = textPaint.measureText(date)
        val maxTextWidth = countWidth.coerceAtLeast(dateWidth)
        val rectHeight = textSize * 2 + textPadding * 3
        checkNotNull(canvas)
        val rect: RectF
        if (canvas.width - posX < maxTextWidth + textPadding * 2) {
            rect = RectF(
                posX - maxTextWidth - textPadding * 2,
                posY,
                posX,
                posY - rectHeight
            )
            canvas.drawRect(posX - cornerRadius, posY, posX, posY - cornerRadius, paint)
        } else {
            rect = RectF(
                posX,
                posY,
                posX + maxTextWidth + textPadding * 2,
                posY - rectHeight
            )
            canvas.drawRect(posX, posY, posX + cornerRadius, posY - cornerRadius, paint)
        }
        canvas.drawRoundRect(
            rect,
            cornerRadius,
            cornerRadius,
            paint
        )
        canvas.drawText(
            count,
            rect.centerX() - countWidth / 2,
            textPaint.getTextBaselineByCenter(rect.centerY()) - rectHeight / 4,
            textPaint
        )
        canvas.drawText(
            date,
            rect.centerX() - dateWidth / 2,
            textPaint.getTextBaselineByCenter(rect.centerY()) + rectHeight / 4,
            textPaint
        )
    }

    private fun Paint.getTextBaselineByCenter(center: Float) = center - (descent() + ascent()) / 2
}
