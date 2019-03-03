package com.ahadar.wordphabet.decorators.colordecorator

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahadar.wordphabet.ListsProvider
import com.ahadar.wordphabet.R

class TopHeaderAdapter(
    private val colours: List<String> = ListsProvider.COLOURS_LIST_HEX
) : RecyclerView.Adapter<TopHeaderAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.colour_item, parent,false) as TextView)

    override fun getItemCount() = colours.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            holder.bind(colours[position])


    class ViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        internal fun bind(hexColour: String) {
            textView.text = hexColour
            textView.setBackgroundColor(Color.parseColor(hexColour))
        }
    }
}