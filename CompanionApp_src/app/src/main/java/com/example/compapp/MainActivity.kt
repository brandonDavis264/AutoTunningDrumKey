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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            if(bluetoothAdapter.isEnabled){
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(this, "Bluetooth disabled", Toast.LENGTH_SHORT).show();
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.BLUETOOTH_ADMIN),
                        1001
                    )
                    bluetoothAdapter.enable()

                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, 1)
                    bluetoothAdapter.startDiscovery()
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN)
                    ActivityResultContracts.RequestPermission()
                }
            }
        }

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        // connect to a bluetooth device acting as a client
        val address = "00:1A:7D:DA:71:13" // just a random value
        val btDevice = bluetoothAdapter?.getRemoteDevice(address)




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

// Create a BroadcastReceiver for ACTION_FOUND.
private val receiver = object : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action
        when(action) {
            BluetoothDevice.ACTION_FOUND -> {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = device?.name ?:"Unknown Device" // handle null case
                val deviceHardwareAddress = device?.address ?: "Unknown address"// MAC address

                // Log or handle the found device info
                println("Device found: $deviceName ($deviceHardwareAddress)")
            }
        }
    }
}


//fun onDestroy() {
//    super.onDestroy()
//
//    // Don't forget to unregister the ACTION_FOUND receiver.
//    unregisterReceiver(receiver)
//}