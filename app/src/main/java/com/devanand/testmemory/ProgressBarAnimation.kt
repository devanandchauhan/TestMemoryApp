package com.devanand.testmemory

import android.content.Context
import android.content.Intent
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import android.widget.TextView

class ProgressBarAnimation: Animation {

    private lateinit var context: Context
    private lateinit var progressBar: ProgressBar
    private lateinit var tvHome:TextView
    private var from: Float = 0.0f
    private var to: Float = 0.0f

    constructor(context: Context,progressBar: ProgressBar,tvHome:TextView,from : Float, to : Float){
        this.context = context
        this.progressBar = progressBar
        this.from = from
        this.to = to
        this.tvHome = tvHome
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        super.applyTransformation(interpolatedTime, t)

        var value = from + (to - from) * interpolatedTime
        progressBar.setProgress((value.toInt()))
        tvHome.setText("${value.toInt()} %")

        if(value == to){
            var intent = Intent(context, HomeActivity::class.java)
            context.startActivity(intent)

        }


    }

}