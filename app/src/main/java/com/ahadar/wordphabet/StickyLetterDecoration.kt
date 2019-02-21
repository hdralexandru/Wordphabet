@file:Suppress("JoinDeclarationAndAssignment")

package com.ahadar.wordphabet

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Typeface
import android.text.TextPaint
import android.view.View
import androidx.core.view.get
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView

/**
 * A class that will draw sticky header.
 */
class StickyLetterDecoration(
    context: Context,
    words: List<String> = WordList.WORDS
) : RecyclerView.ItemDecoration() {

    private val textPaint: TextPaint
    private val positionToInitialsMap: Map<Int, String>
    private val relativeCoordinates: Point<Float>

    private val itemPadding: Float

    init {
        val viewTextSize = context.resources.getDimensionPixelSize(R.dimen.word_text_size)
        positionToInitialsMap = WordList.buildMapWithIndexes(words)
        textPaint = TextPaint(ANTI_ALIAS_FLAG).apply {
            alpha = 255 //Totally visible
            typeface = Typeface.DEFAULT_BOLD // Bold text for better visibility
            textSize = viewTextSize * SCALING_FACTOR
            color = context.resources.getColor(R.color.colorPrimary)
            textAlign = Paint.Align.CENTER
        }
        val itemViewPadding = context.resources.getDimensionPixelOffset(R.dimen.word_padding)
        relativeCoordinates = Point(0f, 0f).apply {
            val rawPixelsMargin = context.resources.getDimensionPixelSize(R.dimen.word_left_margin)
            /* Since we also use textAlign = CENTER, x will be
             * center of our text
             */
            x = rawPixelsMargin / 2f
            /*
             * When using canvas.draw(text, x, y, paint), y represents the BASELINE,
             * not the center, like in x
             */
            val baseLine = (itemViewPadding * 2 + viewTextSize) / 2 + textPaint.textSize / 2
            y = baseLine
        }
        itemPadding = itemViewPadding + (textPaint.textSize - viewTextSize)/2
    }


    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        var prevHeaderY = Float.MAX_VALUE
        var prevFoundPosition = NO_POSITION

        for (initialsPosition in (parent.size - 1) downTo 0) {
            /*
             * Iterate over each item, in reverse order, to be able to
             * `push` the first item upwards
             */
            val currentChild = parent.getChildAt(initialsPosition)
            if (childOutsideParent(currentChild, parent)) continue

            val childPosition: Int = parent.getChildAdapterPosition(currentChild)
            positionToInitialsMap[childPosition]?.let {
                /*
                 * If the position of the current child is a key in our map it means
                 * that this is the first letter of our set of words. We should draw the
                 * initial on the screen
                 */
                val yDrawingPosition = (currentChild.top + currentChild.translationY + relativeCoordinates.y)
                    .coerceAtLeast(relativeCoordinates.y)
                    .coerceAtMost(prevHeaderY - relativeCoordinates.y - itemPadding)
                canvas.drawText(it, relativeCoordinates.x, yDrawingPosition, textPaint)
                prevFoundPosition = childPosition
                prevHeaderY = yDrawingPosition
            }
        }

        /**
         * If no header word was found, it means that
         * the first word of our category and the first word of NEXT category
         * are not visible on screen. For this, we get the current position of the adapter + 1
         * and will later print the first value smaller than this
         */
        prevFoundPosition = if (prevFoundPosition != NO_POSITION) prevFoundPosition else
            parent.getChildAdapterPosition(parent[0]) + 1

        for (initialsPosition in positionToInitialsMap.keys.reversed()) {
            if (initialsPosition < prevFoundPosition) {
                /**
                 * this is the header item of our category
                 */
                positionToInitialsMap[initialsPosition]?.let {
                    val yDrawingPosition = (prevHeaderY - textPaint.textSize - itemPadding)
                        .coerceAtMost(relativeCoordinates.y)
                    canvas.drawText(it, relativeCoordinates.x, yDrawingPosition, textPaint)
                }
                break
            }
        }

    }

    /**
     * Since we iterate over the full map, if a child (possible header) is outside
     * the recyclerView, we shouldn't continue do things, because they won't be visible
     * on screen.
     *
     * Even if the RecyclerView only creates as many items as it needs (and not a full list),
     * we don't know for sure if there are as many items as they fit on the screen or + (1 or 2) extra items
     */
    private fun childOutsideParent(childView: View, parent: RecyclerView): Boolean {
        return childView.bottom < 0
                || (childView.top + childView.translationY.toInt() > parent.height)

    }


    companion object {
        private const val SCALING_FACTOR = 1.5F
        private const val NO_POSITION = -1
    }
}