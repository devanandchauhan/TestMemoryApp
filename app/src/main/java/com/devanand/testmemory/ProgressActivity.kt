package com.devanand.testmemory

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import kotlinx.android.synthetic.main.progress_bar_layout.*
import kotlin.time.toDuration

class ProgressActivity : AppCompatActivity() {

    lateinit var progressBar: ProgressBar
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progress_bar_layout)


        progressBar = findViewById(R.id.progress_bar)
        textView = findViewById(R.id.tvHome)

        progressBar.setMax(100)
        progressBar.setScaleY(3f)

        progressAnimation()

        animation.setAnimation(R.raw.thumbsup)
        animation.playAnimation()


    }

    fun progressAnimation(){
        var progressBarAnimation = ProgressBarAnimation(this,progressBar,textView,0f,100f)
        progressBarAnimation.setDuration(8000)
        progressBar.setAnimation(progressBarAnimation)
    }
}