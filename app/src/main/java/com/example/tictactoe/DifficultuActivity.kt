package com.example.tictactoe

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.databinding.ActivityDifficultyBinding

lateinit var binding: ActivityDifficultyBinding

class DifficultuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDifficultyBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}