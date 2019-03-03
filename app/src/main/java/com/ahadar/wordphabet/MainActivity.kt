package com.ahadar.wordphabet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ahadar.wordphabet.decorators.colordecorator.TopHeaderAdapter
import com.ahadar.wordphabet.decorators.colordecorator.TopHeaderDecoration
import com.ahadar.wordphabet.decorators.stickyinitials.SimpleStickyLetterDecoration
import com.ahadar.wordphabet.decorators.stickyinitials.StickyLetterDecoration
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Wordphabet"
//        hexColourRW()
        wordsAdapter()
    }

    fun hexColourRW() {
        recylerview.adapter = TopHeaderAdapter()
        recylerview.addItemDecoration(TopHeaderDecoration(context = applicationContext))
    }

    fun wordsAdapter () {
        recylerview.adapter = WordsAdapter(ListsProvider.WORDS)

//        recylerview.addItemDecoration(StickyLetterDecoration(context = applicationContext))
        recylerview.addItemDecoration(SimpleStickyLetterDecoration(this))
    }

}
