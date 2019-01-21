package com.ahadar.wordphabet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WordsAdapter(
    private val itemsList: MutableList<String> = mutableListOf()
) : RecyclerView.Adapter<WordsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(itemsList[position], position)

    override fun getItemCount() = itemsList.size

    /* ViewHolders, ClickListener below */
    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(string: String, position: Int) {
            (view as TextView).text = string
            view.setBackgroundColor(view.resources.getColor(if (position % 2 == 0) R.color.fade1 else R.color.fade2))
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(string: String)
    }
}