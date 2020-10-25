package com.lsorter.detection.layer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.lsorter.detection.detectors.LegoBrickDetector

class LegoGraphic constructor(
    overlay: GraphicOverlay, private val legoBrick: LegoBrickDetector.DetectedLegoBrick
) : GraphicOverlay.Graphic(overlay) {

    private val boxPaint = Paint()
    private val textPaint = Paint()

    init {
        boxPaint.color = Color.WHITE
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = STROKE_WIDTH
        textPaint.textSize = TEXT_SIZE
        textPaint.color = Color.WHITE
    }

    override fun draw(canvas: Canvas) {
        val lineHeight = TEXT_SIZE + STROKE_WIDTH

        getBoundaries().let { rect ->
            canvas.drawRect(rect, boxPaint)

            legoBrick.label?.let {
                canvas.drawText(
                    "%s - %.2f%%".format(it.text, it.confidence * 100),
                    rect.left,
                    rect.top - lineHeight,
                    textPaint
                )
            }
        }
    }

    private fun getBoundaries(): RectF {
        val rect = RectF(legoBrick.boundingBox)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        return rect
    }

    companion object {
        private const val TEXT_SIZE = 54.0f
        private const val STROKE_WIDTH = 4.0f
    }
}
