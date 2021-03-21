package com.lsorter.analyze.layer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.lsorter.analyze.common.RecognizedLegoBrick

class LegoGraphic constructor(
    overlay: GraphicOverlay, private val legoBrick: RecognizedLegoBrick
) : GraphicOverlay.Graphic(overlay) {

    private val boxPaint = Paint()
    private val textPaint = Paint()
    private val textIndexPaint = Paint()

    init {
        boxPaint.color = Color.WHITE
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = STROKE_WIDTH
        textPaint.textSize = TEXT_SIZE
        textPaint.color = Color.WHITE
        textIndexPaint.textSize = 2 * TEXT_SIZE
        textIndexPaint.color = Color.WHITE
    }

    override fun draw(canvas: Canvas) {
        getBoundaries().let { rect ->
            canvas.drawRect(rect, boxPaint)

            legoBrick.label?.let {
                canvas.drawText(
                    "%s - %.2f%%".format(it.text, it.confidence * 100),
                    rect.left,
                    rect.top + STROKE_WIDTH + TEXT_SIZE,
                    textPaint
                )

                if (it.index != -1)
                    canvas.drawText(
                        it.index.toString(),
                        ((rect.left + rect.right) - textIndexPaint.measureText(it.index.toString())) / 2,
                        ((rect.top + rect.bottom) - (textIndexPaint.descent() + textIndexPaint.ascent())) / 2,
                        textIndexPaint
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
