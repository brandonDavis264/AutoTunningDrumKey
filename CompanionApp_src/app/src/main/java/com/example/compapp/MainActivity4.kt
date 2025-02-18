package com.example.compapp

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.widget.ArrayAdapter
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.compapp.ui.theme.CompAppTheme
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import kotlin.math.cos
import kotlin.math.sin

class MainActivity4 : ComponentActivity() {
    private val radius = 300
    private var currentRotation = 0f
    private var listeningThread: Thread? = null
    private var isListening = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        val noteTuneTo: Spinner = findViewById(R.id.note)

        val noteTuneOptions = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        fun getDrumFrequency(note: String): Float {
            return when (note) {
                "C" -> 130.81f  // Example frequency in Hz
                "C#" -> 138.59f
                "D" -> 146.83f
                "D#" -> 155.56f
                "E" -> 164.81f
                "F" -> 174.61f
                "F#" -> 185.00f
                "G" -> 196.00f
                "G#" -> 207.65f
                "A" -> 220.00f
                "A#" -> 233.08f
                "B" -> 246.94f
                else -> 0.0f // Default case
            }
        }
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, noteTuneOptions)
        noteTuneTo.adapter = adapter2

        val selectedNote: String = noteTuneTo.selectedItem.toString()
        val targetFrequency = getDrumFrequency(selectedNote)
        //sendCommandToESP(toString())


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

        val capture: Button = findViewById(R.id.captureSection)
        val sect: ImageView = findViewById(R.id.section)

        startListening()

        capture.setOnClickListener {
            sendCommandToESP('s')
            sect.setImageResource(R.drawable.greentri) //Just turn the thing Green!
        }

        val nxt: Button = findViewById(R.id.nxtSection)

        nxt.setOnClickListener {
            if (curLug % 2 == 0)  {
                currentRotation = (currentRotation + 180f) % 360
            }
            else {
                currentRotation = (currentRotation + 180f + (360/lugCount)) % 360
            } // Ensure rotation stays within 0-360
            sect.setImageResource(R.drawable.redtri)
            curLug = (curLug + 1) % lugCount
            val animator = ObjectAnimator.ofFloat(sect, "rotation", currentRotation)
            animator.duration = 300
            animator.interpolator = LinearInterpolator()
            animator.start()
        }
    }

    //https://stackoverflow.com/questions/49402001/how-to-set-visibility-in-kotlin


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

    private fun spawnButtons(frameLayout: FrameLayout, count: Int) {
        val centerX = frameLayout.width / 2
        val centerY = frameLayout.height / 2

        for (i in 0 until count) {
            val angle = i * 2 * Math.PI / count
            val x = (centerX + radius * cos(angle)).toFloat()
            val y = (centerY + radius * sin(angle)).toFloat()

            val button = Button(this).apply {
                text = "Button ${i + 1}"
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply{
                    leftMargin = (x).toInt() - 100
                    topMargin = (y).toInt() - 50
                }
            }
            frameLayout.addView(button)
        }
    }
}