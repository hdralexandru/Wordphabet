package com.ahadar.wordphabet.decorators.colordecorator

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.view.View
import android.view.WindowManager
import androidx.core.view.get
import androidx.core.view.size
import androidx.recyclerview.widget.RecyclerView
import com.ahadar.wordphabet.ListsProvider.HEADERS_COLOURS
import com.ahadar.wordphabet.Point
import com.ahadar.wordphabet.R

class TopHeaderDecoration(
    context: Context,
    private val headersColoursMap: Map<Int, Pair<String, String>> = HEADERS_COLOURS
) : RecyclerView.ItemDecoration() {
    private val textPaint: TextPaint
    private val relativeCoordinates: Point<Float>

    private val itemPadding: Float

    private val headerRectHeight: Float
    private val screenWidth: Float

    init {
        val viewTextSize = context.resources.getDimensionPixelSize(R.dimen.word_text_size)
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = 255 //Totally visible
            typeface = Typeface.DEFAULT_BOLD // Bold text for better visibility
            textSize = context.resources.getDimension(R.dimen.word_text_size)
            textAlign = Paint.Align.CENTER
        }
        val itemViewPadding = context.resources.getDimensionPixelOffset(R.dimen.word_padding)
        relativeCoordinates = Point(0f, 0f).apply {

        }
        itemPadding = itemViewPadding + (textPaint.textSize - viewTextSize) / 2


        headerRectHeight = context.resources.getDimensionPixelOffset(R.dimen.header_height).toFloat()

        // Get screen width
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay
        screenWidth = display.width.toFloat()

    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val childPosition = parent.getChildAdapterPosition(view)
        if (childPosition < 0) return

        if (headersColoursMap.containsKey(childPosition)) {
            outRect.top = headerRectHeight.toInt()
        }
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        var lastFoundPosition: Int = NO_POSITION
        var prevHeaderTop: Float = Float.MAX_VALUE

        for (headerPosition in (parent.size - 1) downTo 0) {
            val view = parent.getChildAt(headerPosition)
            if (childOutsideParent(view, parent)) continue

            val childPosition = parent.getChildAdapterPosition(view)

            headersColoursMap[childPosition]?.let {
                val bottomOfHeader = view.top.toFloat()
                    .coerceAtLeast(headerRectHeight)
                    .coerceAtMost(prevHeaderTop)
                // left - top - right - bottom
                drawHeaderRect(canvas, it, bottomOfHeader)
                lastFoundPosition = childPosition
                prevHeaderTop = bottomOfHeader
            }
        }


        lastFoundPosition = if (lastFoundPosition != NO_POSITION) lastFoundPosition else
            parent.getChildAdapterPosition(parent[0]) + 1

        for (initialsPosition in headersColoursMap.keys.reversed()) {
            if (initialsPosition < lastFoundPosition) {
                /**
                 * this is the header item of our category
                 */
                headersColoursMap[initialsPosition]?.let {
                    val bottom = headerRectHeight
                        .coerceAtMost(prevHeaderTop - headerRectHeight)
                    drawHeaderRect(canvas, it, bottom)
                }
                break
            }
        }
    }

    private fun drawHeaderRect(canvas: Canvas, headerPair: Pair<String, String>, bottom: Float) {
        textPaint.color = Color.WHITE
        canvas.drawRect(0f, (bottom - headerRectHeight), screenWidth, bottom, textPaint)
        textPaint.color = Color.parseColor(headerPair.second)
        canvas.drawText(headerPair.first, screenWidth / 2, bottom - headerRectHeight * .25f, textPaint)
    }


    private fun childOutsideParent(childView: View, parent: RecyclerView): Boolean {
        return childView.bottom < 0
                || (childView.top + childView.translationY.toInt() > parent.height)

    }


    companion object {
        private const val NO_POSITION = -1
    }

}