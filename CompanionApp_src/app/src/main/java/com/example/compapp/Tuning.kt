package com.example.compapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.io.IOException
import kotlin.math.cos
import kotlin.math.sin

class Tuning : ComponentActivity() {
    private val radius = 250
    private var currentRotation = 0f
    private var listeningThread: Thread? = null
    private var isListening = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        val noteTuneTo: Spinner = findViewById(R.id.note)
        val initialN: TextView = findViewById(R.id.initialNote)

        val noteTuneOptions = listOf("B3")
        fun getDrumFrequency(note: String): Float {
            return when (note) {
                "B3" -> 250.00f
                else -> 0.0f // Default case

            }
        }
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, noteTuneOptions)
        noteTuneTo.adapter = adapter2

        noteTuneTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedNote = noteTuneOptions[position]
                val targetFrequency = getDrumFrequency(selectedNote)
                sendCommandToESP(targetFrequency.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }



        val frameLayout: FrameLayout = findViewById(R.id.frameLayout)
        val lugCount = intent.getIntExtra("lugCount", 8) // replace with user input value
        var curLug = 0

        frameLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                frameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                spawnButtons(frameLayout, lugCount)
            }
        })

//        val capture: Button = findViewById(R.id.captureSection)
//        val sect: ImageView = findViewById(R.id.section)

        startListening()

//        capture.setOnClickListener {
//            sendCommandToESP("s")
////            sect.setImageResource(R.drawable.greentri)
//        }

//        val nxt: Button = findViewById(R.id.nxtSection)

//        nxt.setOnClickListener {
//            if (curLug % 2 == 0)  {
//                currentRotation = (currentRotation + 180f) % 360
//            }
//            else {
//                currentRotation = (currentRotation + 180f + (360/lugCount)) % 360
//            } // Ensure rotation stays within 0-360
////            sect.setImageResource(R.drawable.redtri)
//            curLug = (curLug + 1) % lugCount
//            val animator = ObjectAnimator.ofFloat(sect, "rotation", currentRotation)
//            animator.duration = 300
//            animator.interpolator = LinearInterpolator()
//            animator.start()
//        }
    }

    private fun sendCommandToESP(command: String) {
        val bluetoothSocket = AppBluetoothManager.bluetoothSocket

        if (bluetoothSocket != null && bluetoothSocket.isConnected) {
            try {
                val outputStream = bluetoothSocket.outputStream
                outputStream.write((command + "\n").toByteArray())
                outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to send command", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Bluetooth is not connected", Toast.LENGTH_SHORT).show()
        }
    }

    fun freqToNote(note: Float, tolerance: Float = 1.0f): String {
        val noteMap = mapOf(
            "G2" to 98.00f,
            "G#2" to 103.83f,
            "A2" to 110.00f,
            "A#2" to 116.54f,
            "B2" to 123.47f,
            "C3" to 130.81f,
            "C#3" to 138.59f,
            "E3" to 164.81f,
            "F3" to 174.61f,
            "F#3" to 185.00f,
            "G3" to 196.00f,
            "G#3" to 207.65f,
            "A3" to 220.00f,
            "A#3" to 233.08f,
            "B3" to 246.94f,
            "C4" to 261.63f,
            "C#4" to 277.18f,
            "D4" to 293.66f,
            "D#4" to 311.13f,
            "E4" to 329.63f,
            "F4" to 349.23f,
            "F#4" to 369.99f,
            "G4" to 392.00f,
            "G#4" to 415.30f,
            "A4" to 440.00f
        )

        for ((noteName, freq) in noteMap) {
            if (note in (freq - tolerance)..(freq + tolerance)) {
                return noteName
            }
        }

        return "N/A" // Default case
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
            val currentN: TextView = findViewById(R.id.currentNote)

            while (isListening) {
                try {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val receivedData = String(buffer, 0, bytesRead).trim()
//                        runOnUiThread {
//                            // Debugging logs
//                            Toast.makeText(this, "Received: $receivedData", Toast.LENGTH_SHORT).show()
//                        }
                        currentN.text = freqToNote(receivedData.toFloat())
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    isListening = false
                }
            }
        }
        listeningThread?.start()
    }

    private fun spawnButtons(frameLayout: FrameLayout, count: Int) {
        val centerX = frameLayout.width / 2
        val centerY = frameLayout.height / 2
        var selectedButton: Button? = null  // Track the currently selected button

        for (i in 0 until count) {
            val angle = i * 2 * Math.PI / count
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)

            val buttonSize = 70

            val button = Button(this).apply {
                text = "L ${i + 1}"
                setTextColor(Color.LTGRAY)
                setBackgroundResource(R.drawable.diamond) // Default background
                layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize)
            }

            button.setOnClickListener {

                selectedButton?.setBackgroundResource(R.drawable.diamond)


                if (selectedButton == button) {
                    selectedButton = null
                } else {
                    button.setBackgroundResource(R.drawable.diamond_p)
                    selectedButton = button
                }
            }

            val params = FrameLayout.LayoutParams(buttonSize, buttonSize)
            params.leftMargin = (x - buttonSize / 2).toInt()
            params.topMargin = (y - buttonSize / 2).toInt()

            button.layoutParams = params
            frameLayout.addView(button)
        }
    }


}