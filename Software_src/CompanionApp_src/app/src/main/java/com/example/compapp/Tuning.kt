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
import androidx.appcompat.widget.SwitchCompat
import java.io.IOException
import kotlin.math.cos
import kotlin.math.sin
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog


//https://stackoverflow.com/questions/70757388/alertdialog-in-android-studio-kotlin-not-displaying
private fun tutorialDialog1(context: Context) {
    val tutorial = AlertDialog.Builder(context)
    tutorial.apply {
        setTitle("Is this your first time using this application?")
        setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
            tutorialDialog2(context)
        }
        setNegativeButton("No") { _: DialogInterface?, _: Int ->

        }
    }.create().show()
}

private fun tutorialDialog2(context: Context) {
    val tutorial = AlertDialog.Builder(context)
    tutorial.apply {
        setTitle("Set Target Note")
        setMessage("To start, select a target note for your Drum")
        setPositiveButton("Next") { _: DialogInterface?, _: Int ->
            tutorialDialog3(context)
        }
    }.create().show()
}

private fun tutorialDialog3(context: Context) {
    val tutorial = AlertDialog.Builder(context)
    tutorial.apply {
        setTitle("Select Lug")
        setMessage("Choose a Lug to start tuning with. Place the key in the same position")
        setPositiveButton("Next") { _: DialogInterface?, _: Int ->
            tutorialDialog4(context)
        }
    }.create().show()
}

private fun tutorialDialog4(context: Context) {
    val tutorial = AlertDialog.Builder(context)
    tutorial.apply {
        setTitle("Open Mic and Start Playing")
        setMessage("Turn on the Open Mic switch and start playing. Be sure to mute the other " +
                "sections of the drum")
        setPositiveButton("Next") { _: DialogInterface?, _: Int ->
            tutorialDialog5(context)
        }
    }.create().show()
}

private fun tutorialDialog5(context: Context) {
    val tutorial = AlertDialog.Builder(context)
    tutorial.apply {
        setTitle("Repeat and Follow Pattern")
        setMessage("Once tuned, select the next lug, " +
                "place the key in position, and follow the pattern to complete drum tuning!")
        setPositiveButton("Close Tutorial") { _: DialogInterface?, _: Int ->

        }
    }.create().show()
}

class Tuning : AppCompatActivity() {
    private val radius = 250
    private var listeningThread: Thread? = null
    private var isListening = false
    private lateinit var bottomActionBar: TextView
    private lateinit var drum: ImageView
    private var targetHit = false
    private var targetFrequency = 0.0f
    private var currNote = 0.0f
    private var selectedButton: Button? = null
    private var switch: SwitchCompat? = null
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val four_lugs = mutableListOf(45.0f, 135.0f, 225.0f, 315.0f)
    private val six_lugs = mutableListOf(60.0f, 120.0f, 180.0f, 240.0f, 300.0f, 360.0f)
    private val eight_lugs = mutableListOf(67.5f, 112.5f, 157.5f, 202.5f, 247.5f, 292.5f, 337.5f, 382.5f)
    private val ten_lugs = mutableListOf(72.0f, 108.0f, 144.0f, 180.0f, 216.0f, 252.0f, 288.0f, 324.0f, 360.0f, 36.0f)
    private val twelve_lugs = mutableListOf(75.0f, 105.0f, 135.0f, 165.0f, 195.0f, 225.0f, 255.0f, 285.0f, 315.0f, 345.0f, 375.0f, 405.0f)

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

        drum = findViewById(R.id.snare_default)

        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, noteTuneOptions)
        noteTuneTo.adapter = adapter2

        noteTuneTo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedNote = noteTuneOptions[position]
                targetFrequency = getDrumFrequency(selectedNote)
                sendCommandToESP(targetFrequency.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val frameLayout: FrameLayout = findViewById(R.id.frameLayout)
        val lugCount = intent.getIntExtra("lugCount", 8)
        frameLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                frameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (lugCount == 4) {
                    spawnButtons(frameLayout, lugCount, four_lugs)
                } else if (lugCount == 6) {
                    spawnButtons(frameLayout, lugCount, six_lugs)
                } else if (lugCount == 8) {
                    spawnButtons(frameLayout, lugCount, eight_lugs)
                } else if (lugCount == 10) {
                    spawnButtons(frameLayout, lugCount, ten_lugs)
                } else if (lugCount == 12) {
                    spawnButtons(frameLayout, lugCount, twelve_lugs)
                }
            }
        })

        switch = findViewById(R.id.switch2)
        switch?.setOnCheckedChangeListener { _, isChecked ->
            isListening = isChecked
            if (isChecked) {
                startListening()
            } else {
                stopListening()
            }
        }

        val backbutton = findViewById<Button>(R.id.back)
        backbutton.setOnClickListener {
            val socket = AppBluetoothManager.bluetoothSocket
            try {
                socket?.apply {
                    if (isConnected) {
                        close()
                        AppBluetoothManager.bluetoothSocket = null
                        Toast.makeText(this@Tuning, "Disconnected", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@Tuning, "Error disconnecting", Toast.LENGTH_SHORT).show()
            }

            val intent = Intent(this@Tuning, Drum_Specs::class.java)
            startActivity(intent)
            finish() // remove Tuning activity from back stack
        }
        tutorialDialog1(this)
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
            "Select" -> 0.0f
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

    private fun calculateColor(curr: Float, target: Float): Int {
        val maxDiff = 50f
        val diff = kotlin.math.abs(curr - target)

        if (diff <= 10f) {
            targetHit = true
            switch?.isChecked = false
            Toast.makeText(this, "Target note reached!", Toast.LENGTH_SHORT).show()
            return Color.rgb(0, 180, 0)
        } else {
            val fraction = (diff - 10f) / (maxDiff - 10f)
                .coerceIn(0f, 1f) // clamp between 0 and 1

            val red = 255
            val green = (255 * fraction).toInt()

            return Color.rgb(red, green, 0)
        }
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

    private fun startListening()  {
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
                            currNote = receivedData.toFloat()
                            runOnUiThread {
                                val formattedFrequency = String.format("%.2f", receivedData.toFloatOrNull() ?: 0f)
                                Toast.makeText(this, "Received: $formattedFrequency Hz", Toast.LENGTH_SHORT).show()

                                if (switch?.isChecked == true) {
                                    currentNote.text = detectedNote
                                } else {
                                    currentNote.text = "-"
                                }

                                selectedButton?.let {
                                    val color = calculateColor(currNote, targetFrequency)
                                    it.backgroundTintList = ColorStateList.valueOf(color)
                                }
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

    private fun stopListening() {
        isListening = false
        listeningThread?.interrupt()
        listeningThread = null
    }

    private var pulsingButton: Button? = null // Track currently pulsing button
//    private var scalingButton: Button? = null // Track currently scaling button

    private fun spawnButtons(frameLayout: FrameLayout, count: Int, pos: MutableList<Float>) {
        val centerX = frameLayout.width / 2
        val centerY = frameLayout.height / 2
        //var selectedButton: Button? = null

        val star = starPattern(count) // Map<String, List<String>>
        val buttonMap = mutableMapOf<String, Button>() // Stores button text -> Button reference

        var expectedSequence: List<String>? = null
        var sequenceIndex = 0

        for (i in 0 until count) {
            val angle = i * 2 * Math.PI / count
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)

            val buttonSize = 70
            val buttonText = "L ${i + 1}"

            val button = Button(this).apply {
                text = buttonText
                setTextColor(Color.WHITE)
                setBackgroundResource(R.drawable.diamond)
                layoutParams = FrameLayout.LayoutParams(buttonSize, buttonSize)
            }

            buttonMap[buttonText] = button // Store reference

            button.setOnClickListener {
                Log.d("DEBUG", "Button ${button.text} clicked")
                if (targetFrequency == 0.0f) {
                    Toast.makeText(this@Tuning, "Select a target frequency", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                currNote = 3000.0f

                val color = calculateColor(currNote, targetFrequency)
                it.backgroundTintList = ColorStateList.valueOf(color)

                button.setBackgroundResource(R.drawable.r_back)

                runOnUiThread {
                    if (expectedSequence == null) {
                        if (star.containsKey(buttonText)) {
                            expectedSequence = star[buttonText]
                            sequenceIndex = 0
                            Log.d("DEBUG", "Starting new sequence: $expectedSequence")
                        } else {
                            Log.d("DEBUG", "Invalid starting button: $buttonText")
                            return@runOnUiThread
                        }
                    }

                    if (sequenceIndex < expectedSequence!!.size) {
                        val expectedText = expectedSequence!![sequenceIndex]

                        if (buttonText == expectedText) {
                            Log.d("DEBUG", "Correct press: $buttonText (Step: $sequenceIndex)")

                            // Reset the previous button
                            selectedButton?.let {
                                stopPulsingGlowEffect()
//                                stopScalingEffect(it)
//                                it.setBackgroundResource(R.drawable.diamond)
                            }

                            // Apply "diamond_p" background and scaling to the **current** button
//                            button.setTextColor(Color.BLACK)
//                            button.setBackgroundResource(R.drawable.diamond_p)
//                            applyScalingEffect(button)

                            // Update the selected button reference
                            selectedButton = button

                            var lugtext = selectedButton?.text
                            var lugnum = lugtext?.get(2)

                            if (count == 4) {
                                drum.setImageResource(R.drawable.snare_quarter)
                            } else if (count == 6) {
                                drum.setImageResource(R.drawable.snare_sixth)
                            } else if (count == 8) {
                                drum.setImageResource(R.drawable.snare_eigth)
                            } else if (count == 10) {
                                drum.setImageResource(R.drawable.snare_tenth)
                            } else if (count == 12) {
                                drum.setImageResource(R.drawable.snare_twelth)
                            }
                            if (lugnum != null) {
                                if (lugtext?.length!! == 4) {
                                    if ((lugtext?.get(3))?.digitToInt() == 0) {
                                        drum.rotation = pos[9]
                                    } else if ((lugtext?.get(3))?.digitToInt() == 1) {
                                        drum.rotation = pos[10]
                                    } else if ((lugtext?.get(3))?.digitToInt() == 2) {
                                        drum.rotation = pos[11]
                                    }
                                } else {
                                    drum.rotation = pos[lugnum.digitToInt() - 1]
                                }
                            }

                            sequenceIndex++ // Move to next button in sequence

                            // Apply pulsing effect to the next button in the sequence
//                            if (targetHit) {
//                                highlightNextButton(buttonMap, expectedSequence, sequenceIndex)
//                            }
                            if (sequenceIndex < expectedSequence!!.size) {
                                highlightNextButton(buttonMap, expectedSequence, sequenceIndex)
                            } else {
                                Log.d("DEBUG", "Sequence completed!")
                                expectedSequence = null
                                sequenceIndex = 0
                            }
                        } else {
                            Log.d("DEBUG", "Incorrect press: $buttonText, expected: $expectedText")
                            resetButtons(buttonMap)
                            expectedSequence = null
                            sequenceIndex = 0
                        }
                    }
                }
            }

            val params = FrameLayout.LayoutParams(buttonSize, buttonSize)
            params.leftMargin = (x - buttonSize / 2).toInt()
            params.topMargin = (y - buttonSize / 2).toInt()

            button.layoutParams = params
            frameLayout.addView(button)
        }
    }


//    private fun applyScalingEffect(button: Button) {
//        stopScalingEffect(button) // Stop previous effect before applying new one
//
//        val scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 1.2f, 1f).apply {
//            duration = 1600
//            repeatMode = ObjectAnimator.REVERSE
//            repeatCount = ObjectAnimator.INFINITE
//        }
//
//        val scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 1.2f, 1f).apply {
//            duration = 1600
//            repeatMode = ObjectAnimator.REVERSE
//            repeatCount = ObjectAnimator.INFINITE
//        }
//
//        val animatorSet = AnimatorSet()
//        animatorSet.playTogether(scaleX, scaleY)
//        animatorSet.start()
//
//        button.tag = animatorSet // Store animation reference in the button
//        scalingButton = button
//    }

    private fun highlightNextButton(buttonMap: Map<String, Button>, sequence: List<String>?, index: Int) {
        if (sequence != null && index < sequence.size) {
            val nextButtonText = sequence[index]
            val nextButton = buttonMap[nextButtonText]

            nextButton?.let { button ->
                stopPulsingGlowEffect() // Stop pulsing on the previous button

                button.setBackgroundResource(R.drawable.diamond_highlight)

                applyPulsingGlowEffect(button)

                pulsingButton = button // Track the currently pulsing button
            }
        }
    }

    // Function to apply a pulsing glow effect to the next button
    private fun applyPulsingGlowEffect(button: Button) {
        stopPulsingGlowEffect() // Stop pulsing effect on any previous button

        val pulseAnimation = ObjectAnimator.ofFloat(button, "alpha", 0.3f, 1f).apply {
            duration = 1600
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
        }

        val animatorSet = AnimatorSet()
        animatorSet.play(pulseAnimation)
        animatorSet.start()

        button.tag = animatorSet // Store animation reference in the button
        pulsingButton = button
    }

    private fun stopScalingEffect(button: Button) {
        (button.tag as? AnimatorSet)?.cancel()
        button.tag = null
        button.scaleX = 1f // Reset size
        button.scaleY = 1f
    }

    private fun stopPulsingGlowEffect() {
        pulsingButton?.let { button ->
            (button.tag as? AnimatorSet)?.cancel() // Stop animation
            button.tag = null
            button.alpha = 1f // Reset transparency

            // Ensure we only reset the previous pulsing button, NOT the next button!
//            if (button != scalingButton) {
//                button.setTextColor(Color.LTGRAY)
//                button.setBackgroundResource(R.drawable.diamond) // Reset only if it's not the active one
//            }
        }
    }

    private fun resetButtons(buttonMap: Map<String, Button>) {
        for (button in buttonMap.values) {
            if (button != pulsingButton) {
                button.setTextColor(Color.LTGRAY)
                button.setBackgroundResource(R.drawable.diamond)

                // Stop pulsing animation if exists
                (button.tag as? AnimatorSet)?.cancel()
                button.tag = null
            }
        }
    }

    private fun starPattern(count: Int): MutableMap<String, List<String>> {
        val pattern = mutableMapOf<String, List<String>>()

        for (i in 0 until count) {
            val sequence = mutableListOf<String>()
            var radial_j = i
            for (j in 0 until count / 2) {
                if ((radial_j + 1) > count) {
                    sequence.add("L ${(radial_j + 1) - count}")
                } else {
                    sequence.add("L ${radial_j + 1}")
                }
                if ((radial_j + 1) + (count / 2) > count) {
                    sequence.add("L ${((radial_j + 1) + (count / 2)) - count}")
                } else {
                    sequence.add("L ${(radial_j + 1) + (count / 2)}")
                }
                radial_j += 1
            }
            pattern["L ${i + 1}"] = sequence
        }
        return pattern
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val socket = AppBluetoothManager.bluetoothSocket
        try {
            socket?.apply {
                if (isConnected) {
                    close()
                    AppBluetoothManager.bluetoothSocket = null
                    Toast.makeText(this@Tuning, "Disconnected", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this@Tuning, "Error disconnecting", Toast.LENGTH_SHORT).show()
        }

        val intent = Intent(this@Tuning, Drum_Specs::class.java)
        startActivity(intent)
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothStateReceiver)
    }
}
