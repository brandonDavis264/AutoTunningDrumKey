package com.example.compapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class Welcome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val navigateButton: Button = findViewById(R.id.getStartedBtn)
        navigateButton.setOnClickListener {
            val intent = Intent(this, Drum_Specs::class.java)
            startActivity(intent)
        }
    }
}
