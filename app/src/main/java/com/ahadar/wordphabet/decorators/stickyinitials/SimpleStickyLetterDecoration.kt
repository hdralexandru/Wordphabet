package com.ahadar.wordphabet.decorators.stickyinitials

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.graphics.withTranslation
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.ahadar.wordphabet.ListsProvider.buildMapWithIndexes
import com.ahadar.wordphabet.R

class SimpleStickyLetterDecoration(
    context: Context
) : RecyclerView.ItemDecoration() {

    private val drawingSpace: Int
    private val textPaint: TextPaint
    private val itemViewPadding: Float
    private val positionToLayoutMap: Map<Int, StaticLayout>

    private fun buildPositionToLayoutMap(): Map<Int, StaticLayout> {
        val map = mutableMapOf<Int, StaticLayout>()
        buildMapWithIndexes().forEach {
            map[it.key] = buildStaticLayout(it.value)
        }
        return map
    }

    init {
        context.resources.apply {
            drawingSpace = getDimensionPixelSize(R.dimen.word_left_margin)
        }
        val viewTextSize = context.resources.getDimensionPixelSize(R.dimen.word_text_size)
//        positionToInitialsMap = ListsProvider.buildMapWithIndexes(words)
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = 255 //Totally visible
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC) // Bold text for better visibility
            textSize = viewTextSize * SCALING_FACTOR // make it a bit bigger
            color = context.resources.getColor(R.color.initials_color)
            textAlign = Paint.Align.CENTER
        }
        itemViewPadding = context.resources.getDimensionPixelOffset(R.dimen.word_padding).toFloat()
        positionToLayoutMap = buildPositionToLayoutMap()
    }


    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {


        var lastFoundPosition = NO_POSITION
        var previousHeaderTop = Float.MAX_VALUE

        for (viewNo in (parent.size - 1) downTo 0) {
            val view = parent.getChildAt(viewNo)
            if (childOutsideParent(view, parent)) continue

            val childPosition: Int = parent.getChildAdapterPosition(view)
            positionToLayoutMap[childPosition]?.let {
                val top = (view.top + view.translationY + itemViewPadding)
                    .coerceAtLeast(itemViewPadding)
                    .coerceAtMost(previousHeaderTop - it.height)
                c.withTranslation(y = top, x = drawingSpace / 4f) {
                    it.draw(c)
                }
                lastFoundPosition = childPosition
                previousHeaderTop = top - itemViewPadding
            }

        }

        if (lastFoundPosition == NO_POSITION) {
            lastFoundPosition = parent.getChildAdapterPosition(parent.getChildAt(0)) + 1
        }

        for (initialsPosition in positionToLayoutMap.keys.reversed()) {
            if (initialsPosition < lastFoundPosition) {
                positionToLayoutMap[initialsPosition]?.let {
                    val top = (previousHeaderTop - it.height)
                        .coerceAtMost(itemViewPadding)
                    c.withTranslation(y = top, x = drawingSpace / 4f) {
                        it.draw(c)
                    }
                }

                break;
            }
        }
    }


    private fun childOutsideParent(childView: View, parent: RecyclerView): Boolean {
        return childView.bottom < 0
                || (childView.top + childView.translationY.toInt() > parent.height)

    }

    private fun buildStaticLayout(text: String): StaticLayout {
        return StaticLayout(text, textPaint, drawingSpace, Layout.Alignment.ALIGN_CENTER, 0f, 0f, false)
    }

    companion object {
        private const val SCALING_FACTOR = 1.5f
        private const val NO_POSITION = -1
    }
}