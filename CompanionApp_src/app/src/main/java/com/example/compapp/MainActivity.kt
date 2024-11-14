package com.example.compapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compapp.ui.theme.CompAppTheme
import kotlinx.coroutines.launch
import kotlin.random.Random

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.Manifest
import android.app.Activity
import android.content.Context


object BluetoothHelper {
    private const val REQUEST_ENABLE_BT = 1
    private const val REQUEST_BLUETOOTH_PERMISSION = 2

    // Function to check and request Bluetooth permission if necessary
    fun checkAndRequestBluetoothPermission(activity: Activity): Boolean {
        return if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_BLUETOOTH_PERMISSION
            )
            false
        } else {
            true
        }
    }

    // Function to enable Bluetooth if permissions are granted
    fun enableBluetooth(activity: Activity) {
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }
}

class MainActivity : ComponentActivity() {
    private val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.getAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BluetoothHelper.checkAndRequestBluetoothPermission(this)) {
            BluetoothHelper.enableBluetooth(this)
        }

        enableEdgeToEdge()
        setContent {
            CompAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GreetingWithButton(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GreetingWithButton(modifier: Modifier = Modifier) {
    // State to track the recording status
    var isRecording by remember { mutableStateOf(false) }

    // Box layout to set the background color
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0066CC)) // Set background color
            .padding(16.dp)
    ) {
        // Column layout to organize text and button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Display recording status text
            Text(
                text = if (isRecording) "Listening..." else "AutoTuner",
                style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White),
                modifier = Modifier.padding(bottom = 32.dp) // Space between text and button
            )

            // Button with toggle functionality
            Button(
                onClick = {
                    // Toggle the recording status
                    isRecording = !isRecording
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp) // Make the button taller
            ) {
                // Toggle button text based on recording status
                Text(text = if (isRecording) "End Recording" else "Start Recording", style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 18.sp), modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingWithButtonPreview() {
    CompAppTheme {
        GreetingWithButton()
    }
}