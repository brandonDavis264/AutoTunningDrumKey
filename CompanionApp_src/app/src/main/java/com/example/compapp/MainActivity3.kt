package com.example.compapp

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class MainActivity3 : AppCompatActivity() {

    private var listeningThread: Thread? = null
    private var isListening = false
    private lateinit var textView1: TextView
    private lateinit var textView2: TextView
    private lateinit var textView3: TextView
    private lateinit var textView4: TextView
    private lateinit var textView5: TextView
    private lateinit var textView6: TextView
    private var currentRotation = 0f

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

        // Initialize TextViews
        textView1 = findViewById(R.id.textView)
        textView2 = findViewById(R.id.textView2)
        textView3 = findViewById(R.id.textView3)
        textView4 = findViewById(R.id.textView4)
        textView5 = findViewById(R.id.textView5)
        textView6 = findViewById(R.id.textView6)

        startListening()

        val capture: Button = findViewById(R.id.captureSection)
        capture.setOnClickListener {
            sendCommandToESP('s') // Send the 's' character to ESP over Bluetooth
        }

        val nxt: Button = findViewById(R.id.nxtSection)
        val sect: ImageView = findViewById(R.id.section)
        nxt.setOnClickListener {
            currentRotation = (currentRotation + 60f) % 360 // Ensure rotation stays within 0-360
            val animator = ObjectAnimator.ofFloat(sect, "rotation", currentRotation)
            animator.duration = 300
            animator.interpolator = LinearInterpolator()
            animator.start()
        }
    }

    private fun sendCommandToESP(command: Char) {
        val bluetoothSocket = AppBluetoothManager.bluetoothSocket

        if (bluetoothSocket != null && bluetoothSocket.isConnected) {
            try {
                val outputStream = bluetoothSocket.outputStream
                outputStream.write(command.code)
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to send command", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Bluetooth is not connected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startListening() {
        val bluetoothSocket = AppBluetoothManager.bluetoothSocket

        if (bluetoothSocket == null || !bluetoothSocket.isConnected) {
            isListening = false
            return
        }

        isListening = true
        listeningThread = Thread {
            val inputStream = bluetoothSocket.inputStream
            val buffer = ByteArray(1024)

            while (isListening) {
                try {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val receivedData = String(buffer, 0, bytesRead).trim()
                        runOnUiThread {
                            // Debugging logs
                            Toast.makeText(this, "Received: $receivedData", Toast.LENGTH_SHORT).show()
                            updateTextViewBasedOnRotation(receivedData)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    isListening = false
                }
            }
        }
        listeningThread?.start()
    }

    private fun updateTextViewBasedOnRotation(data: String) {
        // Identify the target TextView based on the current rotation
        val targetTextView = when (currentRotation) {
            0f -> textView1
            60f -> textView2
            120f -> textView3
            180f -> textView4
            240f -> textView5
            300f -> textView6
            else -> null
        }

        // Update the identified TextView
        targetTextView?.let {
            it.text = "Avg (Hz): $data" // Add descriptive text to the value
        } ?: Toast.makeText(this, "Unknown orientation", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        isListening = false
        listeningThread?.interrupt()
        listeningThread = null
    }
}
