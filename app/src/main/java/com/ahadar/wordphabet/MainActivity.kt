package com.ahadar.wordphabet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.ahadar.wordphabet.model.WordList
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter: WordsAdapter = WordsAdapter(WordList.words)
        recylerview.adapter = adapter

        Log.d("Letters", WordList.groupedByFirstLetter().keys.toString())

    }


}
