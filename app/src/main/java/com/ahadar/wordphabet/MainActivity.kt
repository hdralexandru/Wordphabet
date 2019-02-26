package com.ahadar.wordphabet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ahadar.wordphabet.topdecoration.TopHeaderAdapter
import com.ahadar.wordphabet.topdecoration.TopHeaderDecoration
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hexColourRW()
    }

    fun hexColourRW() {
        recylerview.adapter = TopHeaderAdapter()
        recylerview.addItemDecoration(TopHeaderDecoration(context = applicationContext))
    }

    fun wordsAdapter () {
        recylerview.adapter = WordsAdapter(ListsProvider.WORDS)

        recylerview.addItemDecoration(TopHeaderDecoration(context = applicationContext))
    }

}
