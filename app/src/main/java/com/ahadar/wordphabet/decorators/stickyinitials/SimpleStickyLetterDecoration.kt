package com.ahadar.wordphabet.decorators.stickyinitials

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
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
    private val stickyLetterPadding: Float

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
        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = 255 //Totally visible
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.ITALIC) // Bold text for better visibility
            textSize = viewTextSize * SCALING_FACTOR // make it a bit bigger
            color = context.resources.getColor(R.color.initials_color)
            textAlign = Paint.Align.CENTER
        }
        stickyLetterPadding = context.resources.getDimensionPixelOffset(R.dimen.word_padding).toFloat()
        positionToLayoutMap = buildPositionToLayoutMap()
    }


    /*
        NOTE:
            - ItemDecoration = ID
            - List of words / WordList = WL
            - Leading word = LW
     */
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        /* Those are more like flags to know whether a leading word (a.k.a the first word in a group of words
        * that have the same initial) is on the screen or not. If it is, we save the relative position of it.
        */
        var lastFoundPosition = NO_POSITION
        var previousHeaderTop = Float.MAX_VALUE

        /*
         * We go in an down-top manner, since lower items can push upper items out of the screen.
         * To keep in mind that we do NOT iterate of each item in our list of words, but we iterate
         * over the views in the RecyclerView. This only holds as many items as it needs to fill the screen.
         */
        for (viewNo in (parent.size - 1) downTo 0) {
            /*
             * If the children is not visible, go to the next one
             */
            val view = parent.getChildAt(viewNo)
            if (childOutsideParent(view, parent)) continue

            /*
             * Hooray! Our item is visible on the screen!
             * Now we find it's position in the adapter, which is equivalent with
             * its position in our WL
             */
            val childPosition: Int = parent.getChildAdapterPosition(view)
            /*
             * This is a trick.
             * Remember that we mapped every position of LW from our WL to a specific layout?
             * Well, if the childPosition (position of the current view) is a in our map, it means
             * that a LW is on the screen -> a new category is visible!
             * We must draw the corresponding letter on the screen.
             *
             * I said this was a trick. If childPosition is not in the map,
             * positionToLayoutMap[childPosition] returns null and the code is not further executed.
             * This could be very well rewritten to if (positionToLayoutMap[childPosition] != null) { ... }
             * but Kotlin is beautiful!
             */
            positionToLayoutMap[childPosition]?.let { stickyLetterLayout ->
                /*
                 * "top" represents the relative position, on the screen, in the RecyclerView,
                 * from where we draw the initials. This is the TOP MARGIN.
                 */
                val top = (view.top + view.translationY + stickyLetterPadding)
                    /*
                     * We ensure it is at least "stickyLetterPadding" pixels under the top.
                     * This ensures that the letters stays in the corner.
                     */
                    .coerceAtLeast(stickyLetterPadding)
                    /*
                     * We also make sure that we don't overlap with a previous initial.
                     * This order (coerceAtLeast first and then coerceAtMost) is important!
                     */
                    .coerceAtMost(previousHeaderTop - stickyLetterLayout.height)

                /*
                 * That's all the logic! Now we simply move to the position from where we want to start drawing
                 * with Canvas.withTranslation(...) and draw our initial.
                 */
                canvas.withTranslation(y = top, x = drawingSpace / 4f) {
                    stickyLetterLayout.draw(canvas)
                }

                /*
                 * Since we found a LW, we set our "flags" corresponding to them.
                 * This will help us later.
                 */
                lastFoundPosition = childPosition
                previousHeaderTop = top - stickyLetterPadding
            }

        }

        /*
         * So far so good. Till now, a Initial will be drawn only and only if the LW is visible on the screen.
         * If not, the map will return null and the code will not be executed.
         * To solve this, we must find the category that is currently on the screen.
         */

        /*
         * If the flag is equal to NO_POSITION, it means that no headers where displayed on the screen.
         * In this case, we must find which category are we in and display the right initial.
         * For this to be done, it's enough to find the position in the WL of the first view on the screen
         * ( and add 1, since in the next for we'll be having strictly a smaller value, not smaller OR equal )
         */
        if (lastFoundPosition == NO_POSITION) {
            lastFoundPosition = parent.getChildAdapterPosition(parent.getChildAt(0)) + 1
        }

        /*
         * In this moment, our flag is set to a position. This position is either a header position (the only category on the screen)
         * or between 2 position of LW (from the above if).
         * Anyhow, the logic is this:
         *      - there can only be 1 category that does not its very own LW on the screen.
         *      - we only have to draw the initial for a category that does not have the LW on the screen, but there are
         *      words from the very same category on the screen. If there are multiple categories on the screen, it means that
         *      their LW are also visible, so the first for is responsible for drawing them.
         *      - we iterate over the keys in reverse order. Once a key is strictly smaller than the flag set above,
         *      it means that that our words are from this category. To understand better, think like this:
         *          = let's suppose our map has the values: { 0: "A", 30: "B", 99: "C" ... }
         *          = we can only have maximum 10 items on the screen.
         *          = the very first item in the recyclerView is on the 45th position in the WL. This means it is a word
         *          starting with "B".
         *          = in this case, "lastFoundPosition" is equal to 46.
         *          = we iterate in reverse order over the keys.
         *              ->  99 < 46? NO! Go to the next one
         *              ->  30 < 46? YES! Then draw the view StaticLayout with the key 30 and exit after
         */
        for (initialsPosition in positionToLayoutMap.keys.reversed()) {
            if (initialsPosition < lastFoundPosition) {
                positionToLayoutMap[initialsPosition]?.let {
                    // The very same logic as in the first for.
                    val top = (previousHeaderTop - it.height)
                        .coerceAtMost(stickyLetterPadding)
                    // We don't need "coerceAtLeast" because the position is directly relative to previous item.
                    canvas.withTranslation(y = top, x = drawingSpace / 4f) {
                        it.draw(canvas)
                    }
                }

                // Stop since we don't have a "higher" position item on the screen.
                break
            }
        }
    }


    private fun childOutsideParent(childView: View, parent: RecyclerView): Boolean {
        return childView.bottom < 0
                || (childView.top + childView.translationY.toInt() > parent.height)

    }

    private fun buildStaticLayout(text: String): StaticLayout {
        return StaticLayout(text, textPaint, drawingSpace, Layout.Alignment.ALIGN_CENTER, 1f, 0f, false)
    }

    companion object {
        private const val SCALING_FACTOR = 1.5f
        private const val NO_POSITION = -1
    }
}