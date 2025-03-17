package com.example.compapp

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import kotlin.math.cos
import kotlin.math.sin

class Tuning : AppCompatActivity() {
    private val radius = 250
    private var currentRotation = 0f
    private var listeningThread: Thread? = null
    private var isListening = false
    private lateinit var bottomActionBar: TextView
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                val deviceName = SelectedBluetoothDevice?.name ?: "Unknown Device"
                val deviceAddress = SelectedBluetoothDevice?.address ?: "No Address"

                val connectivityStatus = when (state) {
                    BluetoothAdapter.STATE_OFF -> "$deviceName: $deviceAddress [Disconnected]"
                    BluetoothAdapter.STATE_ON -> "$deviceName: $deviceAddress [Connected]"
                    else -> "$deviceName: $deviceAddress [Unknown State]"
                }


                runOnUiThread {
                    bottomActionBar.text = connectivityStatus
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)
        supportActionBar?.title = "Tuning"

        bottomActionBar = findViewById(R.id.bottomActionBar)

        // Register BroadcastReceiver for Bluetooth state changes
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)

        // Initial Bluetooth state update
        updateBluetoothStatus()

        val noteTuneTo: Spinner = findViewById(R.id.note)
        val noteTuneOptions = listOf("Select", "B3", "D#4")
        val targetNote: TextView = findViewById(R.id.TargetNote)

        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, noteTuneOptions)
        noteTuneTo.adapter = adapter2

        noteTuneTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedNote = noteTuneOptions[position]
                //val targetFrequency = getDrumFrequency(selectedNote)
                //sendCommandToESP(targetFrequency.toString())
                targetNote.text = selectedNote
                sendCommandToESP(selectedNote)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val frameLayout: FrameLayout = findViewById(R.id.frameLayout)
        val lugCount = intent.getIntExtra("lugCount", 8)
        frameLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                frameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                spawnButtons(frameLayout, lugCount)
            }
        })

        startListening()
    }

    private fun updateBluetoothStatus() {
        val deviceName = SelectedBluetoothDevice?.name ?: "Unknown Device"
        val deviceAddress = SelectedBluetoothDevice?.address ?: "No Address"

        val status = if (bluetoothAdapter?.isEnabled == true) {
            "$deviceName: $deviceAddress [Connected]"
        } else {
            "$deviceName: $deviceAddress [Disconnected]"
        }

        bottomActionBar.text = status
    }

    private fun getDrumFrequency(note: String): Float {
        return when (note) {
            "B3" -> 250.00f
            "D#4" -> 311.13f
            else -> 0.0f
        }
    }

    private fun freqToNote(note: Float): String {
        val noteMap = mapOf(
            "G2" to 98.00f, "G#2" to 103.83f, "A2" to 110.00f, "A#2" to 116.54f,
            "B2" to 123.47f, "C3" to 130.81f, "C#3" to 138.59f, "E3" to 164.81f,
            "F3" to 174.61f, "F#3" to 185.00f, "G3" to 196.00f, "G#3" to 207.65f,
            "A3" to 220.00f, "A#3" to 233.08f, "B3" to 246.94f, "C4" to 261.63f,
            "C#4" to 277.18f, "D4" to 293.66f, "D#4" to 311.13f, "E4" to 329.63f,
            "F4" to 349.23f, "F#4" to 369.99f, "G4" to 392.00f, "G#4" to 415.30f,
            "A4" to 440.00f
        )

        return noteMap.minByOrNull { (_, frequency) -> kotlin.math.abs(frequency - note) }?.key ?: "N/A"
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
            val currentNote: TextView = findViewById(R.id.currentNote)

            while (isListening) {
                try {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val receivedData = String(buffer, 0, bytesRead).trim()
                        val frequency = receivedData.toFloatOrNull() ?: 0f

                        if (frequency > 97.99) {
                            val detectedNote = freqToNote(receivedData.toFloat())
                            runOnUiThread {
                                val formattedFrequency = String.format("%.2f", receivedData.toFloatOrNull() ?: 0f)
                                Toast.makeText(this, "Received: $formattedFrequency Hz", Toast.LENGTH_SHORT).show()

                                currentNote.text = detectedNote
                            }
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
        var selectedButton: Button? = null

        for (i in 0 until count) {
            val angle = i * 2 * Math.PI / count
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)

            val buttonSize = 70

            val button = Button(this).apply {
                text = "L ${i + 1}"
                setTextColor(Color.LTGRAY)
                setBackgroundResource(R.drawable.diamond)
                layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize)
            }

            button.setOnClickListener {
                selectedButton?.setTextColor(Color.LTGRAY)
                selectedButton?.setBackgroundResource(R.drawable.diamond)
                
                if (selectedButton == button) {
                    selectedButton = null
                } else {
                    button.setTextColor(Color.BLACK)
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

//    private fun starPattern(count: Int): MutableMap<String, List<String>> {
//        val starPattern = mutableMapOf<String, List<String>>()
//
//        for (i in 0 until count) {
//            val sequence = listOf<String>()
//            for (j in i until count) {
//
//            }
//            starPattern["L ${i + 1}"] =
//        }
//
//        return starPattern
//    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothStateReceiver)
    }
}
