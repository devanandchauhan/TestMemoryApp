package com.devanand.testmemory

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.devanand.testmemory.models.BoardSize
import com.devanand.testmemory.utils.EXTRA_BOARD_SIZE


class HomeActivity : AppCompatActivity(){

    public lateinit var tvEasy: TextView
    public lateinit var tvMedium:TextView
    public lateinit var tvHard:TextView
    private lateinit var boardSize: BoardSize
    private lateinit var context:Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tvEasy = findViewById(R.id.tv_easy)
        tvMedium = findViewById(R.id.tv_medium)
        tvHard = findViewById(R.id.tv_hard)

        tvEasy.setOnClickListener {
            var intent =  Intent(this,MainActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE,BoardSize.EASY)
            startActivity(intent)

        }
        tvMedium.setOnClickListener{
            var intent =  Intent(this,MainActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE,BoardSize.MEDIUM)
            startActivity(intent)
        }
        tvHard.setOnClickListener {
            var intent =  Intent(this,MainActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE,BoardSize.HARD)
            startActivity(intent)
        }

    }

}