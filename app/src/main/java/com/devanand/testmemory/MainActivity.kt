package com.devanand.testmemory

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devanand.testmemory.models.BoardSize
import com.devanand.testmemory.models.MemoryGame
import com.devanand.testmemory.models.UserImageList
import com.devanand.testmemory.utils.EXTRA_BOARD_SIZE
import com.devanand.testmemory.utils.EXTRA_GAME_NAME
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    companion object{
        private const val CREATE_REQUEST_CODE = 200
    }

    private lateinit var rvBoard:RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var clRoot: ConstraintLayout
    private val db = Firebase.firestore
    private var gameName: String? = null
    private var customGameImages: List<String>? = null

    private var boardSize: BoardSize = BoardSize.EASY
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        var value = intent.getSerializableExtra(EXTRA_BOARD_SIZE)

        boardSize = value as BoardSize
        setupBoard(boardSize)
    }

    private fun setupBoard(boardSize: BoardSize) {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = gameName ?: getString(R.string.app_name)
        when(boardSize){
            BoardSize.EASY -> {

                tvNumMoves.text = "Easy: 4 x 2"
                tvNumPairs.text = "Pairs: 0/4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "Medium: 6 x 3"
                tvNumPairs.text = "Pairs: 0/9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text = "Hard: 6 x 4"
                tvNumPairs.text = "Pairs: 0/12"
            }
        }

        tvNumPairs.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize, customGameImages)

        adapter = MemoryBoardAdapter(this,boardSize, memoryGame.cards, object : MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position)
            }

        })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this,boardSize.getWidth())

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.mi_refresh ->{
                if(memoryGame.getNumMoves() >0 && !memoryGame.haveWonGame()){
                    showAlertDialog("Quit your current game?",null,View.OnClickListener {
                        setupBoard(boardSize)
                    })
                }else{
                    setupBoard(boardSize)
                }

            }

            R.id.mi_new_size->{
                showNewSizeDialog()
                return true
            }
            R.id.mi_custom ->{
                showCreationDialog()
                return true
            }

            android.R.id.home ->{
                showAlertDialog("Quit your current game?",null,View.OnClickListener {
                    finish()
                })
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
            if(customGameName == null){
                Log.e(TAG,"Got null custom game from CreateActivity")
                return
            }

            downloadGame(customGameName)
        }
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun downloadGame(customGameName: String) {
        db.collection("games").document(customGameName).get().addOnSuccessListener {
            document ->
            val userImageList = document.toObject(UserImageList::class.java)
            if(userImageList?.images == null){
                Log.e(TAG,"Invaild custom game from firestore")
                Snackbar.make(clRoot,"Sorry, we couldn't find any such game, '$gameName'",Snackbar.LENGTH_LONG).show()
                return@addOnSuccessListener
            }
            val numCards = userImageList.images.size * 2
            boardSize = BoardSize.getByValue(numCards)
            customGameImages = userImageList.images
            setupBoard(boardSize)
            gameName = customGameName
        }.addOnFailureListener{exception->
            Log.e(TAG,"Exception when retrieving game",exception)
        }
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        showAlertDialog("Create your own memory board",boardSizeView,View.OnClickListener {
            //Set a new value for board size
           val desiredBoardSize: BoardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy-> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }

            //Navigate to new activity
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE,desiredBoardSize)
            @Suppress("DEPRECATION")
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        when (boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)

        }
        showAlertDialog("Choose new size",boardSizeView,View.OnClickListener {
            //Set a new value for board size
            boardSize = when (radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy-> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            gameName = null
            customGameImages = null
            setupBoard(boardSize)
        })
    }

    private fun showAlertDialog(title:String, view: View?,positiveClickListener:View.OnClickListener) {
        AlertDialog.Builder(this).setTitle(title).setView(view).setNegativeButton("Cancel",null)
            .setPositiveButton("Ok"){_,_->
                positiveClickListener.onClick(null)
            }.show()
    }

    private fun updateGameWithFlip(position: Int) {
        //Error checking
        if(memoryGame.haveWonGame()){
            //Alert the user of an invalid move
            Snackbar.make(clRoot,"You already won!!",Snackbar.LENGTH_LONG).show()
            return
        }

        if(memoryGame.isCardFaceUp(position)){
            //Alert the user of an invaild move
            Snackbar.make(clRoot,"Invalid move!",Snackbar.LENGTH_SHORT).show()
            return
        }

        //Actually flip over the cards
        if(memoryGame.flipCard(position)){
            Log.i(TAG,"Found a match! Num pairs found: ${memoryGame.numPairFound}")

            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairFound.toFloat()/boardSize.getNumPairs(),
                ContextCompat.getColor(this,R.color.color_progress_none),
                ContextCompat.getColor(this,R.color.color_progress_full)
            ) as Int

            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "Pairs: ${memoryGame.numPairFound} / ${boardSize.getNumPairs()}"

            if(memoryGame.haveWonGame()){
                Snackbar.make(clRoot,"You won! Congratulations",Snackbar.LENGTH_LONG).show()
                CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.YELLOW,Color.GREEN,Color.MAGENTA)).oneShot()
                
            }
        }
        tvNumMoves.text ="Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}